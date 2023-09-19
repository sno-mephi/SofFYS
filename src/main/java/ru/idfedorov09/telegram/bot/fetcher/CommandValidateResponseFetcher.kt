package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

/**
 * Фетчер обрабатывающий сообщения-команды и заносящий информацию о них в контекст,
 * а также определяет, валидная ли команда
 */
class CommandValidateResponseFetcher : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
    ): UserResponse? {
        val message = updatesUtil.getText(update)?.lowercase()
        val command = message?.split(" ")?.get(0)
        val chatId = updatesUtil.getChatId(update)

        if (message == null || chatId == null) return null

        val isOtherCommand = ResponseAction.entries
            .mapNotNull { it.textForm }
            .contains(command)

        val userResponse = when {
            isProblemSelect() -> UserResponse(
                getUserInfo(),
                UserResponseType.MESSAGE_RESPONSE,
                ResponseAction.SELECT_PROBLEM,
            )
            isAnswerToProblem() -> UserResponse(
                getUserInfo(),
                getAnswerType(update),
                ResponseAction.SEND_ANSWER,
            )
            isOtherCommand -> UserResponse(
                getUserInfo(),
                UserResponseType.MESSAGE_RESPONSE,
                ResponseAction.valueOf(command!!),
            )
            else -> null
        }

        // если есть userResponse, то команда валидная -> ставим флажок
        return userResponse?.also { exp.IS_VALID_COMMAND = true }
    }

    // TODO: дописать (регулярки)
    private fun isProblemSelect(): Boolean {
        return false
    }

    // TODO: дописать (регулярки / проверка на isReply)
    private fun isAnswerToProblem(): Boolean {
        return false
    }

    // TODO: дописать
    private fun getAnswerType(update: Update): UserResponseType {
        return UserResponseType.REPLY_RESPONSE
    }

    // TODO: попробовать потянуть из бд, если пусто то зарегать!
    private fun getUserInfo(): UserInfo {
        return UserInfo()
    }
}
