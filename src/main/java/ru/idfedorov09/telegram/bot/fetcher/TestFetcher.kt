package ru.idfedorov09.telegram.bot.fetcher

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.entity.TelegramPollingBot
import ru.idfedorov09.telegram.bot.enums.BotStage
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

class TestFetcher(
    private val marker: String = "default",
) : GeneralFetcher() {

    @InjectData
    fun doFetch(
        update: Update,
        bot: TelegramPollingBot,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
    ): Update {
        val chatId: String = updatesUtil.getChatId(update)
        val message: String = updatesUtil.getText(update)

        val sent = bot.execute(SendMessage(chatId, "[$marker] test fetcher run... ⏳"))
        val editMessageText = EditMessageText()
        editMessageText.chatId = chatId
        editMessageText.messageId = sent.messageId
        editMessageText.text = "[$marker] test fetcher finished! ✅"
        Thread.sleep(1500L)
        bot.execute(editMessageText)
        bot.execute(SendMessage(chatId, "[$marker] $message; isGame: ${exp.botStage}"))

        update.message?.text = "test edit text!"
        exp.botStage = BotStage.REGISTRATION
        return update
    }
}
