package ru.idfedorov09.telegram.bot.fetcher

import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

class DayProblemFetcher(
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {
    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        val chatId = updatesUtil.getChatId(update) ?: return
        val message = updatesUtil.getText(update)?.lowercase()

        val hashPhoto = update.message.document.fileId

        if (message == "/dp") {
            if ((chatId != "473458128") and (chatId != "920061911")) return
            userInfoRepository.findAll().forEach { _ ->
                bot.execute(
                    SendPhoto().also {
                        it.chatId = chatId
                        it.photo = InputFile(hashPhoto)
                    },
                )
            }
        } else {
            if (update.message.hasDocument() or update.message.hasPhoto() or update.message.hasText()) {
                val msg = ForwardMessage()
                msg.fromChatId = chatId
                msg.chatId = "920061911"
                msg.messageId = update.message.messageId
            }
        }
    }
}
