package ru.idfedorov09.telegram.bot.fetcher.global

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

@Component
class GlobalRegistrationFetcher(
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        userInfo: UserInfo,
        bot: TelegramPollingBot,
    ) {
        val message = updatesUtil.getText(update)?.lowercase() ?: return
        val chatId = updatesUtil.getChatId(update) ?: return

        when {
            userInfo.studyGroup == null -> groupNumEnterStage(userInfo, message, chatId, bot)
            userInfo.fullName == null -> fullNameEnterStage(userInfo, message, chatId, bot)
            else -> return // уже базово зареган
        }
    }

    private fun groupNumEnterStage(
        userInfo: UserInfo,
        groupNumber: String,
        chatId: String,
        bot: TelegramPollingBot,
    ) {
        if (!isCorrectStudyGroup(groupNumber)) {
            bot.execute(
                SendMessage(
                    chatId,
                    "Некорректный номер группы",
                ),
            )
            return
        }

        userInfoRepository.save(userInfo.copy(studyGroup = groupNumber))
        bot.execute(
            SendMessage(
                chatId,
                "Введи свое ФИО :)",
            ),
        )
    }

    private fun fullNameEnterStage(
        userInfo: UserInfo,
        fullName: String,
        chatId: String,
        bot: TelegramPollingBot,
    ) {
        userInfoRepository.save(userInfo.copy(fullName = fullName))
        bot.execute(
            SendMessage(
                chatId,
                "Спасибо за регистрацию!"
            )
        )
    }

    private fun isCorrectStudyGroup(input: String): Boolean {
        val regex = Regex("[бс]\\d{2}-\\d{3}")
        return regex.matches(input)
    }
}
