package ru.idfedorov09.telegram.bot.fetcher

import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

class MCFetcher() {
    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        bot: TelegramPollingBot,
    ) {
        val chatId =
        if ((chatId != 473458128L) and (chatId != 920061911L))
        val message = updatesUtil.getText(update)?.lowercase()
    }
}
