package ru.idfedorov09.telegram.bot.flow

/**
 * Объект контекста флоу, содержащий информацию о работающих фичах, режимах и тд и тп
 */
data class ExpContainer(
    /**
     * Флаги для примера!
     */
    val isRegistration: Boolean = false,
    val isGame: Boolean = false,
    val isAppeal: Boolean = false,
    val isOffline: Boolean = false,
)
