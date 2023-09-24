package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.UserInfoService

@Component
class PoolFetcher(
    private val userInfoService: UserInfoService,
    private val problemRepository: ProblemRepository,
) : GeneralFetcher() {

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ) {
        val problemsPool = userResponse.initiatorTeam?.problemsPool ?: return
        var i = 1
        problemsPool.forEach { probId ->
            val problem = problemRepository.findById(probId).get()

            val tui = userResponse.initiator.tui ?: return

            bot.execute(SendMessage(tui, "i" + "'${problem.category} ${problem.cost}'").also { it.enableMarkdown(true) })

            i++
        }
    }
}
