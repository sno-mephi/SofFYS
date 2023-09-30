package ru.idfedorov09.telegram.bot.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.data.GlobalSets
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class GameTimeCheckService(
    private val redisService: RedisService,
    private val bot: TelegramPollingBot,
    private val userInfoRepository: UserInfoRepository,
) {

    companion object {
        private const val POLYAKOV_TRASH_ID = "473458128"
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @Scheduled(fixedRate = 500)
    private fun check() {
        val startGameTime = redisService.getSafe(PropertyNames.START_GAME_TIME)
        startGameTime?.let {
            val currentTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val endGameTime =
                LocalDateTime.parse(startGameTime, formatter).plusMinutes(GlobalSets.GAME_DURATION_IN_MINUTES)
            val timeDifference = Duration.between(currentTime, endGameTime)
            if (timeDifference.isNegative || timeDifference.isZero) {
                // TODO: игра кончилась
                redisService.setValue(PropertyNames.START_GAME_TIME, null)
                redisService.setValue(PropertyNames.STAGE_PROPERTY, BotGameStage.APPEAL.name)
                userInfoRepository.findAll()
                    .filter { it.isCaptain && it.tui != null }
                    .forEach {
                        bot.execute(
                            SendMessage(
                                it.tui!!,
                                "Игра закончилась. Наступает этап апелляций. Вот что нужно делать..",
                            ),
                        )
                        Thread.sleep(300)
                    }
            }
        }
    }
}
