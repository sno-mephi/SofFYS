package ru.idfedorov09.telegram.bot.flow

import ru.idfedorov09.telegram.bot.enums.BotStage

/**
 * Объект контекста флоу, содержащий информацию о работающих фичах, режимах и тд и тп
 */
@Mutable
data class ExpContainer(
    var botStage: BotStage = BotStage.OFFLINE
)
