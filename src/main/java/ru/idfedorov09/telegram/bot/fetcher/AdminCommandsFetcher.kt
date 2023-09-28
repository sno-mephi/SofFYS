package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.data.enums.ResponseAction
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.reflect

@Component
class AdminCommandsFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val redisService: RedisService,
) : GeneralFetcher() {

    // TODO: проверка корректности переходов между этапами?

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
        private const val ADMIN_ID = "920061911"

        private const val startGameMessageToMember = "Игра начинается! Получчи доску. " +
            "Не забудь предупредить капитана, если он отошел!"
        private const val startGameMessageToCaptain = "Игра начинается! Ваша доска."

        private const val startAppeal = "Начинается апелляция. Если у вас есть то-то.."

        private const val endAppeal = "Прием апелляций завершен"
    }

    @InjectData
    fun doFetch(
        exp: ExpContainer,
        userResponse: UserResponse?,
        bot: TelegramPollingBot,
    ) {
        userResponse ?: return
        if (userResponse.initiator.tui != ADMIN_ID) return

        when (userResponse.action) {
            ResponseAction.START_REGISTRATION -> {
                exp.botGameStage = BotGameStage.REGISTRATION
                exp.registrationStage = RegistrationStage.CAP_REGISTRATION
            }
            ResponseAction.START_TEAMS_INVITE -> {
                exp.botGameStage = BotGameStage.REGISTRATION
                exp.registrationStage = RegistrationStage.TEAM_REGISTRATION
            }
            ResponseAction.START_GAME -> startGame(exp, bot)
            ResponseAction.START_APPEAL -> startAppeal(exp, bot)
            ResponseAction.FINISH_APPEAL -> finishAppeal(exp, bot)
            else -> return
        }

        redisService.setValue(PropertyNames.STAGE_PROPERTY, exp.botGameStage.name)
        redisService.setValue(PropertyNames.STAGE_GAME_REG_PROPERTY, exp.registrationStage.name)
    }

    // TODO: тут на самом деле надо кидать картинки доски, доделать
    private fun sendToAll(
        bot: TelegramPollingBot,
        action: (UserInfo) -> Any?,
    ) {
        when (action.reflect()?.returnType?.javaType) {
            SendMessage::class.java -> userInfoRepository.findAll().forEach {
                it.tui ?: run {
                    log.error("tui not saved for user with id=${it.id}.")
                    return
                }
                val result = action.invoke(it) ?: return@forEach
                bot.execute(result as BotApiMethodMessage)
            }
            SendPhoto::class.java -> userInfoRepository.findAll().forEach {
                it.tui ?: run {
                    log.error("tui not saved for user with id=${it.id}.")
                    return
                }
                val result = action.invoke(it) ?: return@forEach
                bot.execute(result as SendPhoto)
            }
        }
    }

    // TODO: не привысим ли тут ограничение в 30 сообщ в секунду??? Мб сделать паузу? (да на методах других тоже)
    // TODO: попросить у Андрея картинку исходной доски! кидать сюда
    private fun startGame(
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        exp.botGameStage = BotGameStage.GAME
        sendToAll(bot) {
            SendMessage(
                it.tui!!,
                if (it.isCaptain) startGameMessageToCaptain else startGameMessageToMember,
            )
        }
    }

    private fun startAppeal(
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        exp.botGameStage = BotGameStage.APPEAL
        sendToAll(bot) {
            SendMessage(
                it.tui!!,
                startAppeal,
            )
        }
    }

    private fun finishAppeal(
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        exp.botGameStage = BotGameStage.APPEAL
        sendToAll(bot) {
            SendMessage(
                it.tui!!,
                endAppeal,
            )
        }
    }
}
