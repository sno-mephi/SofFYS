package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.IsAnswer
import ru.idfedorov09.telegram.bot.data.model.Team
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.Board.changeBoard
import java.io.File
import kotlin.jvm.optionals.getOrNull

@Component
class AnswerFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val problemRepository: ProblemRepository,
    private val teamRepository: TeamRepository,
) : GeneralFetcher() {

    companion object {
        private const val POLYAKOV_TRASH_ID = "473458128"
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ): IsAnswer {
        if (userResponse.action != ResponseAction.SEND_ANSWER) return IsAnswer()
        val user = userResponse.initiator
        val problemId = userResponse.problemId ?: return IsAnswer()
        val team = userResponse.initiatorTeam ?: return IsAnswer()
        val tui = user.tui ?: return IsAnswer()
        val attemptNumber = userResponse.attemptAnswerNumber ?: 1
        val answer = userResponse.answer?.lowercase() ?: return IsAnswer()
        val teamId = team.id

        if (!user.isCaptain) return IsAnswer()
        if (problemId in team.completedProblems) {
            bot.execute(SendMessage(tui, "Вы уже завершили эту задачу"))
            return IsAnswer()
        }
        if (problemId !in team.problemsPool) {
            bot.execute(SendMessage(tui, "Вы еще не решаете эту задачу"))
            return IsAnswer()
        }
        val isAnswer = problemRepository.findById(problemId).get().isAnswer(answer)
        if (isAnswer) {
            val point = problemRepository.findById(problemId).getOrNull()?.cost?.div(attemptNumber)
            teamRepository.save(team.copy(points = team.points + point!!))
            itIsOver(team, problemId)
            bot.execute(SendMessage(tui, "Вы верно решили задачу, получите $point баллов"))
        } else {
            if (attemptNumber == 2L) {
                itIsOver(team, problemId)
                bot.execute(
                    SendMessage(
                        tui,
                        "Вы израсходавали все попытки," +
                            "правильный ответ: ${problemRepository.findById(problemId).get().answers}",
                    ),
                )
            } else {
                bot.execute(SendMessage(tui, "Неверно! У вас осталасть одна попытка"))
            }
        }
        val boardHash = bot.execute(
            SendPhoto().also {
                it.chatId = POLYAKOV_TRASH_ID
                it.photo = InputFile(File("images/boards/$teamId.png"))
            },
        ).photo.firstOrNull()?.fileId

        teamRepository.save(team.copy(lastBoardHash = boardHash))
        return IsAnswer(isAnswer)
    }
    private fun itIsOver(team: Team, problemId: Long) {
        team.problemsPool.remove(problemId)
        team.completedProblems.add(problemId)
        teamRepository.save(team)
        team.id?.let { toCompleted(it, problemId) }
    }

    private fun toCompleted(teamId: Long, problemId: Long) {
        changeBoard(teamId, problemId, "COMPLETED")
    }
}
