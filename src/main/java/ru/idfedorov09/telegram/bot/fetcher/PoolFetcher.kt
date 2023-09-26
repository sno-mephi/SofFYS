package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.ProblemRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.UserInfoService

@Component
class PoolFetcher(
    private val userInfoService: UserInfoService,
    private val problemRepository: ProblemRepository,
) : GeneralFetcher() {

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ) {
        if (userResponse.action != ResponseAction.GET_POOL) {
            return
        }

        val tui = userResponse.initiator.tui ?: return

        val problemsPool = userResponse.initiatorTeam?.problemsPool ?: run {
            bot.execute(SendMessage(tui, "В пуле нет задач"))
            return
        }
        var i = 1
        var answerMessage = "Задачи в пуле:"
        problemsPool.forEach { probId ->
            val problem = problemRepository.findById(probId).get()
            answerMessage += "\n$i. '${problem.category} ${problem.cost}' (ЛУЛ/2)"
            // добавить вместо ЛУЛ счётчик попыток(сколько осталось)
            i++
        }
        bot.execute(SendMessage(tui, answerMessage).also { it.enableMarkdown(true) })
    }
}
