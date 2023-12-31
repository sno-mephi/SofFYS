package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
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
class NewProblemFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val problemRepository: ProblemRepository,
    private val teamRepository: TeamRepository,
) : GeneralFetcher() {

    companion object {
        private const val POLYAKOV_TRASH_ID = "473458128"
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ) {
        if (userResponse.action != ResponseAction.SELECT_PROBLEM) return
        val user = userResponse.initiator
        val tui = userResponse.initiator.tui ?: return
        val problemId = userResponse.problemId ?: return
        val team = userResponse.initiatorTeam ?: return
        val teamId = team.id
        val problemPhotoHash = problemRepository.findById(problemId).getOrNull()?.problemHash

        problemPhotoHash ?: run {
            log.error("Detected problem with uncorrect / without problemHash!")
            return
        }

        if (problemId in team.problemsPool) {
            bot.execute(
                SendPhoto().also {
                    it.caption = "Эта задача уже есть в пуле"
                    it.chatId = tui
                    it.photo = InputFile(problemPhotoHash)
                },
            )
            return
        }
        if (!user.isCaptain) return
        if (problemId in team.completedProblems) {
            bot.execute(SendMessage(tui, "Вы уже решали эту задачу"))
            return
        }
        if (team.problemsPool.size == 3) {
            bot.execute(SendMessage(tui, "Вы не можете решать больше трех задач одновременно"))
            return
        }
        team.problemsPool.add(problemId)

        userInfoRepository.findAll()
            .filter { it.teamId == teamId }
            .forEach { userInTeam ->
                userInTeam.tui?.let {
                    val photo = SendPhoto()
                    photo.chatId = it
                    photo.photo = InputFile(problemPhotoHash)
                    bot.execute(photo)
                }
            }
        if (teamId != null) {
            toPool(teamId, problemId)
        }

        val boardHash = bot.execute(
            SendPhoto().also {
                it.chatId = POLYAKOV_TRASH_ID
                it.photo = InputFile(File("images/boards/$teamId.png"))
            },
        ).photo?.firstOrNull()?.fileId

        teamRepository.save(team.copy(lastBoardHash = boardHash))
    }

    private fun toPool(teamId: Long, problemId: Long) {
        changeBoard(teamId, problemId, "POOL")
    }
}
