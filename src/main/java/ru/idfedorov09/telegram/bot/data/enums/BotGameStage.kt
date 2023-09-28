package ru.idfedorov09.telegram.bot.data.enums

/**
 * Перечисление с примерами возможного статуса занятности бота
 */
enum class BotGameStage {
    REGISTRATION,
    GAME,
    APPEAL,
    AFTER_APPEAL,
    OFFLINE,
    ;

    companion object {
        fun contains(
            stringValue: String,
        ) = BotGameStage.values().map { it.name }.contains(stringValue)
    }
}
