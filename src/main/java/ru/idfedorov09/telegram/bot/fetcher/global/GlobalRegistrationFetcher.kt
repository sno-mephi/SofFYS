package ru.idfedorov09.telegram.bot.fetcher.global

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher
import ru.idfedorov09.telegram.bot.flow.ExpContainer
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
        exp: ExpContainer,
    ) {
        val message = updatesUtil.getText(update)
        val chatId = updatesUtil.getChatId(update) ?: return

        exp.isRegistered = false
        when {
            userInfo.studyGroup == null -> groupNumEnterStage(userInfo, message?.lowercase(), chatId, bot)
            userInfo.fullName == null -> fullNameEnterStage(userInfo, message, chatId, bot)
            else -> exp.isRegistered = true // уже базово зареган
        }
    }

    private fun groupNumEnterStage(
        userInfo: UserInfo,
        groupNumber: String?,
        chatId: String,
        bot: TelegramPollingBot,
    ) {
        groupNumber ?: return
        if (!isCorrectStudyGroup(groupNumber)) {
            bot.execute(
                SendMessage(
                    chatId,
                    "Введи корректный номер группы для регистрации",
                ),
            )
            return
        }

        userInfoRepository.save(userInfo.copy(studyGroup = groupNumber))
        bot.execute(
            SendMessage(
                chatId,
                "Введи свое ФИО для завершения регистрации!",
            ),
        )
    }

    private fun fullNameEnterStage(
        userInfo: UserInfo,
        fullName: String?,
        chatId: String,
        bot: TelegramPollingBot,
    ) {
        fullName ?: return
        userInfoRepository.save(userInfo.copy(fullName = fullName))

        bot.execute(
            SendMessage(
                chatId,
                "Регистрация завершена! ✔ Добро пожаловать на Школу будущего молодого ученого!",
            ),
        )
    }

    private fun isCorrectStudyGroup(input: String): Boolean {
        val regex = Regex("[абмс]\\d{2}-\\d{3}")
        return regex.matches(input)
    }
}
