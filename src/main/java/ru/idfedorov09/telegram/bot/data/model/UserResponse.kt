package ru.idfedorov09.telegram.bot.data.model

import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import java.time.LocalDateTime

/**
 * Содержит информацию об ответе пользователя
 */
data class UserResponse(
    val initiator: UserInfo, // инициатор ответа
    val initiatorTeam: Team?, // команда инициатора, null если такой нет
    val userResponseType: UserResponseType, // тип ответа
    val action: ResponseAction, // действие
    val receiveTime: LocalDateTime, // время в котрое пришло обновление
    val problemId: Long?, // id задачи на которой завязан ответ; null если не завязан
    val answer: String?, // ответ на задачу, null если это не обновление на ответ
    val message: String? = null, // Сообщение от пользователя(в частности при регистрации), возможно надо изменить, я добавил, чтобы код был рабочим
    val attemptAnswerNumber: Long?, // номер попытки ответа (1 или 2), null если это не ответ
)
