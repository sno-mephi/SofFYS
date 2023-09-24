package ru.idfedorov09.telegram.bot.fetcher

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

class NewProblemFetcher : GeneralFetcher() {

    @InjectData
    fun doFetch(
        userResponse: UserResponse
    ){
        if (userResponse.action != ResponseAction.SELECT_PROBLEM) return
        val user = userResponse.initiator
        val problemId = userResponse.problemId
        val team = user.team()?: return

        if (problemId in team.problemsPool){
            // Пишу что задача уже есть в пуле и отправляю ее картинку
            return
        }
        if (!user.isCaptain) return
        if (problemId in team.completedProblems){
            // Пишу что задача уже решена
            return
        }
        if (team.problemsPool.size == 3){
            // Пишу что пул заполнен
            return
        }
        // Добавляю задачу в пул
        // Отправляю сообщение с картинкой через хэш из таблицы проблемс ВСЕМ УЧАСТНИКАМ
        // Меняю на борде


    }
}