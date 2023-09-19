package ru.idfedorov09.telegram.bot.data.model

import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType

/**
 * Содержит информацию об ответе пользователя
 */
data class UserResponse(
    val initiator: UserInfo, // инициатор ответа
    val userResponseType: UserResponseType, // тип ответа
    val action: ResponseAction, // действие
    // TODO: получить время пришествия сообщения, к какой задаче это применяется и ответ
)
