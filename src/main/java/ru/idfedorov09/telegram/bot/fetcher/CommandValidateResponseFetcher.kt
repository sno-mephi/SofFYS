package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ActionRepository
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.UserInfoService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil
import java.time.LocalDateTime

/**
 * Фетчер обрабатывающий сообщения-команды и заносящий информацию о них в контекст,
 * а также определяет, валидная ли команда
 */
@Component
class CommandValidateResponseFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val userInfoService: UserInfoService,
    private val problemRepository: ProblemRepository,
    private val actionRepository: ActionRepository,
) : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
    ): UserResponse? {
        val currentTime = LocalDateTime.now()
        val message = updatesUtil.getText(update)
        val command = message?.split(" ")?.get(0)
        val chatId = updatesUtil.getChatId(update) ?: return null

        if (
            message == null &&
            !update.hasCallbackQuery() &&
            !(exp.registrationStage == RegistrationStage.CAP_REGISTRATION && message!=null)
        ) {
            return null
        }

        val isOtherCommand = ResponseAction.entries
            .mapNotNull { it.textForm }
            .contains(command)

        val initiator = getUserInfoOrInsertToDb(chatId)
        val initiatorTeam = userInfoService.getTeam(initiator)

        val userResponse = when {
            isAnswerToProblem(message) -> UserResponse(
                initiator = initiator,
                initiatorTeam = initiatorTeam,
                userResponseType = extractAnswerType(update),
                action = ResponseAction.SEND_ANSWER,
                receiveTime = currentTime,
                problemId = extractProblemId(message),
                answer = extractAnswer(message),
                messageText = message,
            )
            isProblemSelect(message) -> UserResponse(
                initiator = initiator,
                initiatorTeam = initiatorTeam,
                userResponseType = UserResponseType.MESSAGE_RESPONSE,
                action = ResponseAction.SELECT_PROBLEM,
                receiveTime = currentTime,
                problemId = extractProblemId(message),
                answer = null,
                messageText = message,
            )
            isOtherCommand -> UserResponse(
                initiator = initiator,
                initiatorTeam = initiatorTeam,
                userResponseType = UserResponseType.MESSAGE_RESPONSE,
                action = ResponseAction.entries.first { it.textForm == command },
                receiveTime = currentTime,
                problemId = extractProblemId(message),
                answer = null,
                messageText = message,
            )
            else -> null
        }

        if (update.hasCallbackQuery() && userResponse == null) {
            exp.IS_VALID_COMMAND = true
            return UserResponse(
                initiator = initiator,
                initiatorTeam = initiatorTeam,
                userResponseType = extractAnswerType(update),
                action = ResponseAction.UNKNOWN_ACTION,
                receiveTime = currentTime,
                problemId = null,
                answer = null,
                messageText = null,
            )
        }

        if (
            exp.botGameStage == BotGameStage.REGISTRATION &&
            exp.registrationStage == RegistrationStage.CAP_REGISTRATION &&
            userResponse == null
        ) {
            exp.IS_VALID_COMMAND = true
            return UserResponse(
                initiator = initiator,
                initiatorTeam = initiatorTeam,
                userResponseType = extractAnswerType(update),
                action = ResponseAction.CREATE_COMMAND,
                receiveTime = currentTime,
                problemId = null,
                answer = null,
                messageText = message,
            )
        }
        // если есть userResponse, то команда валидная -> ставим флажок
        return userResponse?.also { exp.IS_VALID_COMMAND = true }
            .getAttemptAnswerNumber()
    }

    private fun UserResponse?.getAttemptAnswerNumber(): UserResponse? {
        this?.initiatorTeam?.id ?: return this
        this.problemId ?: return this
        val kek = actionRepository.findAll()
        val countAnswers = actionRepository.countAnswer(this.initiatorTeam.id, problemId)
        return this.copy(attemptAnswerNumber = countAnswers)
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
    private fun extractProblemId(
        message: String?,
    ): Long? {
        message ?: return null
        if (!isMessageContainsProblem(message)) return null

        return problemRepository.findAll().find {
            it.category?.lowercase() == message.split(" ")[0].lowercase() &&
                it.cost == message.split(" ")[1].toLongOrNull()
        }?.id
    }

    // TODO: дописать для случая REPLY
    private fun extractAnswer(
        message: String?,
    ): String? {
        return message?.split(" ")?.drop(2)?.joinToString(" ")
    }

    // TODO: дописать для случая REPLY
    private fun isAnswerToProblem(
        message: String?,
    ): Boolean {
        return isMessageContainsProblem(message) && !isProblemSelect(message)
    }

    // TODO: дописать для случая REPLY
    private fun extractAnswerType(update: Update): UserResponseType {
        return if (update.hasCallbackQuery()) UserResponseType.BUTTON_RESPONSE else UserResponseType.MESSAGE_RESPONSE
    }

    private fun getUserInfoOrInsertToDb(
        chatId: String,
    ) = userInfoRepository.findByTui(chatId)
        ?: UserInfo(tui = chatId).also { userInfoRepository.save(it) }
}
