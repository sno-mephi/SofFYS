package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.enums.GlobalStage
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

/**
 * Подтягивает Stage бота из бд. Если не получается, то становится в offline
 */
@Component
class StageResolveFetcher(
    private val redisService: RedisService,
) : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
        private const val ADMIN_ID = "920061911"
    }

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
    ) {
        if (exp.EXP_COMMANDS) {
            setStage(updatesUtil, update, redisService, exp)
        }

        // получаем состояние игры бота
        exp.botGameStage = redisService.getSafe(PropertyNames.STAGE_PROPERTY)
            ?.let { BotGameStage.valueOf(it) }
            ?: BotGameStage.OFFLINE

        // получаем состояние регистрации в игре
        exp.registrationStage = redisService.getSafe(PropertyNames.STAGE_GAME_REG_PROPERTY)
            ?.let { RegistrationStage.valueOf(it) }
            ?: RegistrationStage.NO_REGISTRATION

        // получаем глобальное состояние бота
        exp.globalStage = redisService.getSafe(PropertyNames.GLOBAL_STAGE_PROPERTY)
            ?.let { GlobalStage.valueOf(it) }
            ?: GlobalStage.REGISTRATION

        log.info("exp after stageResolveFetcher: {}", exp)
    }

    /**
     *  Командой /set_stage SOME_STAGE (от админа) устанавливает новое состояние
     */
    private fun setStage(
        updatesUtil: UpdatesUtil,
        update: Update,
        redisService: RedisService,
        exp: ExpContainer,
    ) {
        val message = updatesUtil.getText(update)
        val chatId = updatesUtil.getChatId(update)

        if (message == null || chatId == null) return

        if (
            Regex("/set_stage\\s+\\w+").matches(message) &&
            message.split(" ").size == 2 &&
            BotGameStage.contains(message.split(" ")[1]) &&
            chatId == ADMIN_ID
        ) {
            redisService.setValue(PropertyNames.STAGE_PROPERTY, message.split(" ")[1])
            exp.IS_STAGE_CHANGED = true
            log.info("changed bot stage to ${message.split(" ")[1]}")
        }
    }
}
