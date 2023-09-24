package ru.idfedorov09.telegram.bot.data.enums

enum class ResponseAction(
    val textForm: String?,
) {
    // админские
    DEFAULT_VALUE("default_value"),
    START_REGISTRATION("/reg_start"), // OFF TO REG
    START_GAME("/game_start"), // REG TO GAME
    START_APPEAL("/appeal_start"), // GAME to APPEAL
    FINISH_APPEAL("/appeal_finish"), // APPEAL TO AFTER_APPEAL Конец апелляции, подведение результатов

    // пользовательские вида ```РУБРИКА_БАЛЛ (ответ)``` (или с ответом на сообщение)
    SEND_ANSWER(null),
    SELECT_PROBLEM(null),
    SEND_APPEAL(null),

    // текстовые (или с ответом на сообщение)
    GET_POOL("/pool"),
    GET_BOARD("/board"),
    GET_STATE("/state"),
    GET_TOP("/top"),
}
