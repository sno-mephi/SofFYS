
package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.CountActionsByTeamIdAndProblemIdAndAction
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ActionRepository
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import kotlin.jvm.optionals.getOrNull

@Component
class AdminComfirmApealFetcher(
    private val problemRepository: ProblemRepository,
    private val teamRepository: TeamRepository,
    private val actionRepository: ActionRepository,
) : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
        update: Update,
    ): CountActionsByTeamIdAndProblemIdAndAction {
        val tui = userResponse.initiator.tui ?: return CountActionsByTeamIdAndProblemIdAndAction()

        if (!(exp.botGameStage == BotGameStage.APPEAL || exp.botGameStage == BotGameStage.AFTER_APPEAL)) {
            return CountActionsByTeamIdAndProblemIdAndAction()
        }

        if (!(tui == "920061911" && userResponse.userResponseType == UserResponseType.BUTTON_RESPONSE)) {
            return CountActionsByTeamIdAndProblemIdAndAction()
        }

        val answer = update.callbackQuery.data.split(" ")
        val team = teamRepository.findById(answer.getOrNull(1)?.toLong() ?: -1).getOrNull() ?: return CountActionsByTeamIdAndProblemIdAndAction()
        var messageToAdmin = ""

        val problemCost = problemRepository.findById(answer.getOrNull(2)?.toLong() ?: -1).get().cost ?: return CountActionsByTeamIdAndProblemIdAndAction()
        val successAttempts = actionRepository.findCorrectAnswers(
            answer.getOrNull(1)?.toLong() ?: -1,
            answer.getOrNull(2)?.toLong() ?: -1,
        )

        val oldPoints = when {
            successAttempts.isEmpty() -> 0
            successAttempts.any { it.correctAnswerAttempt == 1L } -> problemCost
            successAttempts.any { it.correctAnswerAttempt == 2L } -> problemCost.div(2)
            else -> 0
        }

        val result = when (answer[0]) {
            "First_true" -> {
                teamRepository.save(team.copy(points = team.points + problemCost - oldPoints))
                messageToAdmin = "Принята ПЕРВАЯ попытка, засчитан полный балл ($problemCost), вычтено $oldPoints"
                CountActionsByTeamIdAndProblemIdAndAction(1)
            }
            "Second_true" -> {
                teamRepository.save(team.copy(points = team.points + problemCost / 2 - oldPoints))
                messageToAdmin = "Принята ВТОРАЯ попытка, засчитана половина баллов (${problemCost / 2}, вычтено $oldPoints)"
                CountActionsByTeamIdAndProblemIdAndAction(2)
            }
            else -> {
                messageToAdmin = "Попытка отклонена"
                CountActionsByTeamIdAndProblemIdAndAction(0)
            }
        }

        bot.execute(
            EditMessageReplyMarkup().also {
                it.chatId = "920061911"
                it.messageId = update.callbackQuery.message.messageId
                it.replyMarkup = InlineKeyboardMarkup(listOf())
            },
        )

        val oldText = update.callbackQuery.message.text
        messageToAdmin = "$oldText\n$messageToAdmin"
        bot.execute(
            EditMessageText().also {
                it.chatId = "920061911"
                it.messageId = update.callbackQuery.message.messageId
                it.text = messageToAdmin
            },
        )

        return result
    }
}
