package ru.idfedorov09.telegram.bot.flow

/**
 * Объект контекста флоу, содержащий информацию о работающих фичах, режимах и тд и тп
 */
@Mutable
data class ExpContainer(
    /**
     * Флаги для примера!
     */
    var isRegistration: Boolean = false,
    var isGame: Boolean = false,
    var isAppeal: Boolean = false,
    var isOffline: Boolean = false,
)
