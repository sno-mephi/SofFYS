package ru.idfedorov09.telegram.bot.data.model

import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import java.time.LocalDateTime

/**
 * Содержит информацию об ответе пользователя
 */
data class UserResponse(
    val initiator: UserInfo, // инициатор ответа
    val userResponseType: UserResponseType, // тип ответа
    val action: ResponseAction, // действие
    val receiveTime: LocalDateTime, // время в котрое пришло обновление
    val problemId: Long?, // id задачи на которой завязан ответ; null если не завязан
    val answer: String?, // id ответа на задачу, null если это не обновление на ответ
)
