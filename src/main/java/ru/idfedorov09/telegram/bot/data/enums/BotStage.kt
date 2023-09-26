package ru.idfedorov09.telegram.bot.data.enums

/**
 * Перечисление с примерами возможного статуса занятности бота
 */
enum class BotStage {
    CAP_REGISTRATION,
    TEAM_REGISTRATION,
    GAME,
    APPEAL,
    AFTER_APPEAL,
    OFFLINE,
    ;

    companion object {
        fun contains(
            stringValue: String,
        ) = BotStage.values().map { it.name }.contains(stringValue)
    }
}
