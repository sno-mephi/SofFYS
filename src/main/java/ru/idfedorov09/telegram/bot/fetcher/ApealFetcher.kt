package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ActionRepository
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData

@Component
class ApealFetcher(
    private val problemRepository: ProblemRepository,
    private val teamRepository: TeamRepository,
    private val userInfoRepository: UserInfoRepository,
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
    ) {
        userResponse.initiator.tui ?: return

        if (!(exp.botGameStage == BotGameStage.APPEAL && userResponse.initiator.isCaptain)) {
            return
        }

        val problemId = userResponse.problemId ?: return
        val team = userResponse.initiatorTeam ?: return

        team.id ?: return

        if (problemId in team.appealedProblems) {
            bot.execute(
                SendMessage(
                    userResponse.initiator.tui,
                    "Ты уже подал апелляцию на эту задачу!",
                ),
            )
            return
        }

        userResponse.initiator.teamId ?: return

        val successAttempts = actionRepository.findCorrectAnswers(userResponse.initiator.teamId, userResponse.problemId)

        if (successAttempts.any { it.correctAnswerAttempt == 1L }) {
            bot.execute(
                SendMessage(
                    userResponse.initiator.tui,
                    "Вы решили эту задачу на полный балл.",
                ),
            )
            return
        }

        val problemCategory = problemRepository.findById(problemId).get().category
        val problemCost = problemRepository.findById(problemId).get().cost
        val realAnswer = problemRepository.findById(problemId).get().answers
        val teamAnswers = actionRepository.findAnswersByTeamIdAndProblemId(team.id, problemId)

        if (teamAnswers.isEmpty()) {
            bot.execute(
                SendMessage(
                    userResponse.initiator.tui,
                    "Вы не решали эту задачу",
                ),
            )
            return
        }

        team.appealedProblems.add(problemId)
        teamRepository.save(team)

        var message = "Команда ${team.teamName}. Задача: $problemCategory $problemCost." +
            "\nПервый ответ команды: ${teamAnswers[0]} "

        if (teamAnswers.size > 1) {
            message += "\nВторой ответ команды: ${teamAnswers[1]} "
        } else { message += "\nВторой ответ команда не давала" }

        message += "\nВерый ответ $realAnswer"
        bot.execute(
            SendMessage(
                "920061911",
                message,
            ).also {
                it.replyMarkup = createChooseKeyboard(userResponse, teamAnswers.size)
            },
        )
    }

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }

    private fun createChooseKeyboard(
        userResponse: UserResponse,
        answersCount: Int,
    ) = createKeyboard(
        listOf(
            mutableListOf(
                InlineKeyboardButton("первый ✅").also { it.callbackData = "First_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
                InlineKeyboardButton("второй ✅").also { it.callbackData = "Second_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
                InlineKeyboardButton("ничего ❌").also { it.callbackData = "No_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
            ).also {
                if (answersCount <= 1) it.removeAt(1)
            },
        ),
    )
}
