package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
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
        val tui = userResponse.initiator.tui ?: return

        if (!(exp.botGameStage == BotGameStage.APPEAL && userResponse.initiator.isCaptain)) {
            return
        }

        val problemId = userResponse.problemId ?: return
        val team = userResponse.initiatorTeam ?: return

        if (!team.id?.let { actionRepository.presenceOfIncorrectAnswers(it, problemId) }!! || problemId in team.appealedProblems) {
            return
        }

        team.appealedProblems.add(problemId)
        teamRepository.save(team)

        val problemCategory = problemRepository.findById(problemId).get().category
        val problemCost = problemRepository.findById(problemId).get().cost
        val realAnswer = problemRepository.findById(problemId).get().answers
        val teamAnswers = actionRepository.findAnswersByTeamIdAndProblemId(team.id, problemId)
        var message = "Команда ${team.teamName}. Задача:$problemCategory $problemCost." +
                "\n Первый ответ команды: ${teamAnswers[0]} "

                    if (teamAnswers.size > 1) {
            message += "\n второй ответ команды: ${teamAnswers[1]} "
        } else { message += "\n второй ответ команды: (нету)" }

        message += "\n верый ответ $realAnswer"
        bot.execute(
            SendMessage(
                "920061911",
                message,
            ).also {
                it.replyMarkup = createChooseKeyboard(userResponse)
            },
        )
    }

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }

    private fun createChooseKeyboard(
        userResponse: UserResponse,
    ) = createKeyboard(
        listOf(
            listOf(
                InlineKeyboardButton("Первый ответ верный ✅").also { it.callbackData = "First_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
                InlineKeyboardButton("Второй ответ верый ✅").also { it.callbackData = "Second_true ${userResponse.initiatorTeam?.id} ${userResponse.problemId}" },
                InlineKeyboardButton("Ничего неверно ❌").also { it.callbackData = "No_true" },

            ),
        ),
    )
}
