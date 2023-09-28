package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

@Component
class DayProblemFetcher(
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        val chatId = updatesUtil.getChatId(update) ?: return
        val message = updatesUtil.getText(update)?.lowercase()

        val hashPhoto =
            try{
                update.message.photo.firstOrNull()?.fileId
            } catch (e: NullPointerException) {
                log.warn("Can't find photo by user answer.")
                null
            }

        if (message == "/dp" && hashPhoto != null) {
            if ((chatId != "473458128") and (chatId != "920061911")) return
            userInfoRepository.findAll().forEach { currentUser ->
                Thread.sleep(100L)
                currentUser.tui ?: return@forEach
                bot.execute(
                    SendPhoto().also {
                        it.chatId = currentUser.tui
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
                bot.execute(
                    SendMessage(
                        "920061911",
                        "Ответ от ${userInfoRepository.findByTui(chatId)?.fullName}",
                    ),
                )
                bot.execute(msg)
            }
        }
    }
}
