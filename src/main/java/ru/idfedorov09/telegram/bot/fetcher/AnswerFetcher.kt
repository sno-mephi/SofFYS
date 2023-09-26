package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.Team
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.util.Board.changeBoard

@Component
class AnswerFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val problemRepository: ProblemRepository,
    private val teamRepository: TeamRepository,
) : GeneralFetcher() {
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ) {
        if (userResponse.action != ResponseAction.SEND_ANSWER) return
        val user = userResponse.initiator
        val problemId = userResponse.problemId ?: return
        val team = userResponse.initiatorTeam ?: return
        val tui = user.tui ?: return
        val attemptNumber = userResponse.attemptAnswerNumber ?: return
        val answer = userResponse.answer ?: return

        if (!user.isCaptain) return
        if (problemId in team.completedProblems) {
            bot.execute(SendMessage(tui, "Вы уже завершили эту задачу"))
            return
        }
        if (problemId !in team.problemsPool) {
            bot.execute(SendMessage(tui, "Вы еще не решаете эту задачу"))
            return
        }
        // Случай если задача решена Верна
        if (problemRepository.findById(problemId).get().isAnswer(answer)) {
            val point = problemRepository.findById(problemId).get().cost?.div(attemptNumber)
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
    }
    private fun itIsOver(team: Team, problemId: Long) {
        team.problemsPool.remove(problemId)
        teamRepository.save(team.copy(problemsPool = team.problemsPool))
        team.completedProblems.add(problemId)
        teamRepository.save(team.copy(completedProblems = team.completedProblems))
        toCompleted(team.id, problemId)
    }

    private fun toCompleted(teamId: Long, problemId: Long) {
        changeBoard(teamId, problemId, "COMPLETED")
    }
}
