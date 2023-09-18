package ru.idfedorov09.telegram.bot.fetcher

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData

/**
 * Выводит некоторую информацию мне в сообщеиния:
 */
class MessageLogFetcher : GeneralFetcher() {

    companion object {
        private val logChatId = "920061911"
    }

    @InjectData
    fun doFetch(
        bot: TelegramPollingBot,
        exp: ExpContainer,
    ) {
        exp.botStage
        bot.execute(
            SendMessage(
                logChatId,
                "current bot state: ${exp.botStage.name}",
            ),
        )
    }
}
