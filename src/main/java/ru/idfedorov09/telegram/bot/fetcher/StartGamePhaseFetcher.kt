package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class StartGamePhaseFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val redisService: RedisService,
) : GeneralFetcher() {

    @InjectData
    fun doFetch(
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        if (!exp.IS_STAGE_CHANGED) return
        val startHashId = redisService.getSafe(PropertyNames.START_BOARD_HASH)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        redisService.setValue(PropertyNames.START_GAME_TIME, LocalDateTime.now(ZoneId.of("Europe/Moscow")).format(formatter))

        userInfoRepository.findAll().forEach { user ->
            user.tui ?: return@forEach
            val message = when (user.isCaptain) {
                false -> "Привет! Началась игра. Это информативное сообщение не капитану"
                true -> "Привет, началась игра. Это информативное сообщение КАПИТАНУ!"
            }
            bot.execute(SendMessage(user.tui, message))
            bot.execute(
                SendPhoto().also {
                    it.chatId = user.tui
                    it.photo = InputFile(startHashId)
                },
            )
            Thread.sleep(500)
        }
    }
}
