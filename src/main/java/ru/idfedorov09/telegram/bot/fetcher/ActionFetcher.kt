
package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.model.Action
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.model.IsAnswer
import ru.idfedorov09.telegram.bot.data.repo.ActionRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ActionFetcher(
    private val actionRepository: ActionRepository,
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
        isAnswer: IsAnswer
    ) {


        val action = Action(
            teamId = userResponse.initiatorTeam?.id,
            time = userResponse.receiveTime,
            action = userResponse.action,
            problemId = userResponse.problemId,
            isCorrectAnswer = isAnswer.isAnswer,
            correctAnswerAttempt = userResponse.attemptAnswerNumber,
        )

        actionRepository.save(action)
    }
}
