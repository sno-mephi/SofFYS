package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
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
    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
    ) {
        val tui = userResponse.initiator.tui ?: return
        var answerMessage = "Статус игры: ${exp.botStage}"
        val startGameTime = redisService.getSafe("start_game_time")
        startGameTime?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val endGameTime = LocalDateTime.parse(startGameTime, formatter).plusMinutes(30)
            val timeDifference = Duration.between(LocalDateTime.now(ZoneId.of("Europe/Moscow")), endGameTime)
            answerMessage += String.format("\nОставшееся время игры: %d минут %d секунд", timeDifference.toMinutes(), timeDifference.seconds % 60)
        }
        bot.execute(SendMessage(tui, answerMessage))
    }
}
