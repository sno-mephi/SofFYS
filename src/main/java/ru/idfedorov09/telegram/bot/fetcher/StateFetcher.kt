package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

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
        var answerMessage = "Статус игры:${exp.botStage}."
        val start_game_time = redisService.getSafe("start_game_time")
        start_game_time?.let {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val end_game_time = LocalDateTime.parse(start_game_time, formatter).plusMinutes(30)
            val current_date_time = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
            val timeDifference = Duration.between(current_date_time, end_game_time)
            val formattedTime = String.format("\nОставшееся время игры:%d минут %d секунд.", timeDifference.toMinutes(), timeDifference.seconds % 60)
            answerMessage += formattedTime
        }

        bot.execute(SendMessage(tui, answerMessage))
    }
}
