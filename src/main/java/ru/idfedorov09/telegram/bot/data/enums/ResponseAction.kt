package ru.idfedorov09.telegram.bot.data.enums

enum class ResponseAction(
    val textForm: String?,
) {
    // админские
    UNKNOWN_ACTION("unknown_action"),
    START_REGISTRATION("/game_reg_start"), // OFF TO REG
    START_TEAMS_INVITE("/teams_invite"),
    START_GAME("/game_start"), // REG TO GAME
    START_APPEAL("/appeal_start"), // GAME to APPEAL
    FINISH_APPEAL("/appeal_finish"), // APPEAL TO AFTER_APPEAL Конец апелляции, подведение результатов
    CLOSE_FULL_REG("/close_reg"),

    // пользовательские вида ```РУБРИКА_БАЛЛ (ответ)``` (или с ответом на сообщение)
    SEND_ANSWER(null),
    SELECT_PROBLEM(null),
    SEND_APPEAL(null),
    CREATE_COMMAND(null),

    // текстовые (или с ответом на сообщение)
    GET_POOL("/pool"),
    GET_BOARD("/board"),
    GET_STATE("/state"),
    GET_TOP("/top"),
}
