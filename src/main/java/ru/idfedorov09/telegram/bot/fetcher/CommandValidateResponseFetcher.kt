package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil
import java.time.LocalDateTime

/**
 * Фетчер обрабатывающий сообщения-команды и заносящий информацию о них в контекст,
 * а также определяет, валидная ли команда
 */
@Component
class CommandValidateResponseFetcher : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    private lateinit var update: Update
    private var message: String? = null

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
    ): UserResponse? {
        val currentTime = LocalDateTime.now()
        val message = updatesUtil.getText(update)?.lowercase()
        val command = message?.split(" ")?.get(0)
        val chatId = updatesUtil.getChatId(update)

        this.update = update
        this.message = message

        // TODO: а что если нажата кнопка? message==null по идее. подумать!
        if (message == null || chatId == null) return null

        val isOtherCommand = ResponseAction.entries
            .mapNotNull { it.textForm }
            .contains(command)

        val userResponse = when {
            isProblemSelect(message) -> UserResponse(
                initiator = getUserInfo(),
                userResponseType = UserResponseType.MESSAGE_RESPONSE,
                action = ResponseAction.SELECT_PROBLEM,
                receiveTime = currentTime,
                problemId = extractProblemId(),
                answer = null,
            )
            isAnswerToProblem() -> UserResponse(
                initiator = getUserInfo(),
                userResponseType = extractAnswerType(),
                action = ResponseAction.SEND_ANSWER,
                receiveTime = currentTime,
                problemId = extractProblemId(),
                answer = extractAnswer(),
            )
            isOtherCommand -> UserResponse(
                initiator = getUserInfo(),
                userResponseType = UserResponseType.MESSAGE_RESPONSE,
                action = ResponseAction.valueOf(command!!),
                receiveTime = currentTime,
                problemId = extractProblemId(),
                answer = null,
            )
            else -> null
        }

        // если есть userResponse, то команда валидная -> ставим флажок
        return userResponse?.also { exp.IS_VALID_COMMAND = true }
    }

    /**
     * Проверка того что сообщение содержит задачу, те имеет вид
     * ```русское_слово число_кратное_100 некоторый текст```
     */
    private fun isMessageContainsProblem(message: String?): Boolean {
        message ?: return false

        val matchResult = Regex("""^\p{IsCyrillic}+\s\d+.*$""").find(message) ?: return false
        val number = matchResult.value.split(" ")[1].toInt()

        return number % 100 == 0 && number in 100..1100
    }

    /**
     * проверяет что сообщение имеет вид
     * ```русское_слово число_кратное_100```
     */
    private fun isProblemSelect(message: String?): Boolean {
        message ?: return false
        if (!isMessageContainsProblem(message)) return false
        return message.matches(Regex("""^\p{IsCyrillic}+\s\d+$"""))
    }

    // TODO: дописать для случая REPLY
    private fun extractProblemId(): Long? {
        message ?: return null
        if (!isMessageContainsProblem(message)) return null

        return message!!.split(" ")[1].toLongOrNull()
    }

    // TODO: дописать для случая REPLY
    private fun extractAnswer(): String? {
        return message?.split(" ")?.drop(2)?.joinToString(" ")
    }

    // TODO: дописать для случая REPLY
    private fun isAnswerToProblem(): Boolean {
        return isMessageContainsProblem(message) && !isProblemSelect(message)
    }

    // TODO: дописать для случая REPLY
    private fun extractAnswerType(): UserResponseType {
        return UserResponseType.MESSAGE_RESPONSE
    }

    // TODO: попробовать потянуть из бд, если пусто то зарегать!
    private fun getUserInfo(): UserInfo {
        return UserInfo()
    }
}
