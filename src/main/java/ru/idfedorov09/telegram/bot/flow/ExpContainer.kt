package ru.idfedorov09.telegram.bot.flow

import ru.idfedorov09.telegram.bot.data.enums.BotStage
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage

/**
 * Объект контекста флоу, содержащий информацию о работающих фичах, режимах и тд и тп
 */
@Mutable
data class ExpContainer(
    var botStage: BotStage = BotStage.OFFLINE, // состояние бота
    var registrationStage: RegistrationStage = RegistrationStage.NO_REGISTRATION,
    var EXP_COMMANDS: Boolean = true, // включение экспериментальных команд
    var IS_VALID_COMMAND: Boolean = false, // валидная ли команда пришла
    var IS_STAGE_CHANGED: Boolean = false, // было ли изменено состояние бота
    var CAP_REGISTRATION_CLOSED_NOW: Boolean = false, // регистрация всех пользователей (В данный момент она закрытаБ открывается один раз за игру!!!
)
