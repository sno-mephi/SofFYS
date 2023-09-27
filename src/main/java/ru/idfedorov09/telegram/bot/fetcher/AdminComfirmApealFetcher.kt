
package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.enums.BotStage
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService

@Component
class AdminComfirmApealFetcher(
    private val problemRepository: ProblemRepository,
    private val redisService: RedisService,
    private val teamRepository: TeamRepository,
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
        update: Update,
    ) {
        val tui = userResponse.initiator.tui ?: return

        if (!(exp.botStage == BotStage.APPEAL || exp.botStage == BotStage.AFTER_APPEAL)) {
            return
        }

        if (!(tui == "920061911" && userResponse.userResponseType == UserResponseType.BUTTON_RESPONSE)) {
            return
        }

        val answer = update.callbackQuery.data.split(" ")
        val team = teamRepository.findById(answer[1].toLong()).get()

       when (answer[0]) {
           "First_true" -> {
               val problemCost = problemRepository.findById(answer[2].toLong()).get().cost ?: return
               teamRepository.save(team.copy(points = team.points + problemCost))
           }
           "Second_true" -> {
               val problemCost = problemRepository.findById(answer[2].toLong()).get().cost ?: return
               teamRepository.save(team.copy(points = team.points + problemCost / 2))
           }
           else -> return
       }
    }
}
