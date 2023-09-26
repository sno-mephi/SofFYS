package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData

@Component
class TopFetcher(
    private val teamRepository: TeamRepository,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
    ) {
        if (userResponse.action != ResponseAction.GET_TOP) {
            return
        }

        val tui = userResponse.initiator.tui ?: return
        var answerMessage = "Топ команд:"

        val teamList = mutableListOf<Pair<String?, Long>>()

        teamRepository.findAll()
            .forEach { team ->
                teamList.add(Pair(team.teamName, team.points))
            }
        val sortedTeamList = teamList.sortedByDescending { it.second }

        var i = 1

        sortedTeamList.forEach { team ->
            if (team.first == userResponse.initiatorTeam?.teamName) {
                answerMessage += "\n$i. **${team.first}**: ${team.second}"
            } else {
                answerMessage += "\n$i. ${team.first}: ${team.second}"
            }
            i++
        }

        bot.execute(SendMessage(tui, answerMessage).also { it.enableMarkdown(true) })
    }
}
