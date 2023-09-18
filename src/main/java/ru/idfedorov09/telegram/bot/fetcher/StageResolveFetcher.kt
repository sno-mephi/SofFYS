package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.BotStage
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

/**
 * Подтягивает Stage бота из бд. Если не получается, то становится в offline
 */
class StageResolveFetcher : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
        redisService: RedisService,
    ) {
        val message: String = updatesUtil.getText(update)

        if (
            Regex("/set_stage\\s+\\w+").matches(message) &&
            BotStage.values().map { it.name }.contains(message.split(" ")[1])
        ) {
            redisService.setValueByKey(PropertyNames.STAGE_PROPERTY, message.split(" ")[1])
        }
        exp.botStage = redisService.getValueByKey(PropertyNames.STAGE_PROPERTY)
            ?.let { BotStage.valueOf(it) }
            ?: BotStage.OFFLINE
    }
}
