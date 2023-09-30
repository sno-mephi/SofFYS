package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import ru.idfedorov09.telegram.bot.data.GlobalSets
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class StateFetcher(
    private val redisService: RedisService,
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
        if (userResponse.action != ResponseAction.GET_STATE) {
            return
        }

        val tui = userResponse.initiator.tui ?: return
        var answerMessage = "Статус игры: ${exp.botGameStage}"
        val startGameTime = redisService.getSafe(PropertyNames.START_GAME_TIME)
        startGameTime?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val endGameTime = LocalDateTime.parse(startGameTime, formatter).plusMinutes(GlobalSets.GAME_DURATION_IN_MINUTES)
            val timeDifference = Duration.between(LocalDateTime.now(ZoneId.of("Europe/Moscow")), endGameTime)
            answerMessage += String.format("\nОставшееся время игры: %d минут %d секунд", timeDifference.toMinutes(), timeDifference.seconds % 60)

            val photoHash = userResponse.initiatorTeam?.lastBoardHash ?: return

            bot.execute(
                SendPhoto().also {
                    it.caption = answerMessage
                    it.chatId = tui
                    it.photo = InputFile(photoHash)
                },
            )
            return
        }

        if (exp.botGameStage == BotGameStage.GAME) {
            log.error("bot state: GAME, but there is no start time (it is NULL)")
            return
        }

        bot.execute(SendMessage(tui, answerMessage))
    }
}
