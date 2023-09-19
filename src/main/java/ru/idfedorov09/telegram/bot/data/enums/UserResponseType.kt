package ru.idfedorov09.telegram.bot.data.enums

/**
 * Типы ответов
 */
enum class UserResponseType {
    MESSAGE_RESPONSE, // сообщение с командой
    BUTTON_RESPONSE, // нажата кнопка
    REPLY_RESPONSE, // ответное сообщение
}