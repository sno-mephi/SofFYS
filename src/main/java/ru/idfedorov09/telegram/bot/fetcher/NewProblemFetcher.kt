package ru.idfedorov09.telegram.bot.fetcher

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

class NewProblemFetcher : GeneralFetcher() {

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ){
        if (userResponse.action != ResponseAction.SELECT_PROBLEM) return
        val user = userResponse.initiator
        val tui = userResponse.initiator.tui ?: return
        val problemId = userResponse.problemId
        val team = userResponse.initiatorTeam?: return

        if (problemId in team.problemsPool){
            bot.execute(SendMessage(tui, "Эта задача уже есть в пуле"))
            // Пишу что задача уже есть в пуле и отправляю ее картинку
            return
        }
        if (!user.isCaptain) return
        if (problemId in team.completedProblems){
            bot.execute(SendMessage(tui, "Вы уже решали эту задачу"))
            // Пишу что задача уже решена
            return
        }
        if (team.problemsPool.size == 3){
            bot.execute(SendMessage(tui, "Вы не можете решать больше трех задач одновременно"))
            // Пишу что пул заполнен
            return
        }

        // Добавляю задачу в пул
        // Отправляю сообщение с картинкой через хэш из таблицы проблемс ВСЕМ УЧАСТНИКАМ
        // Меняю на борде


    }
}