package ru.idfedorov09.telegram.bot.fetcher.global

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.GlobalSets
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.BotGameStage
import ru.idfedorov09.telegram.bot.data.enums.GlobalStage
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.repo.ActionRepository
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil
import java.io.Serializable
import java.util.*

/**
 * Фетчер обрабатывающий текстовые команды от админа
 */
@Component
class GlobalAdminTextCommandsResolveFetcher(
    private val redisService: RedisService,
    private val teamRepository: TeamRepository,
    private val actionRepository: ActionRepository,
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {

    companion object {
        private val adminIds = listOf(
            "473458128",
            "920061911",
        )
    }

    private data class Params(
        val update: Update,
        val updatesUtil: UpdatesUtil,
        val userInfo: UserInfo,
        val bot: TelegramPollingBot,
        val exp: ExpContainer,
        val chatId: String,
        val commandText: String,
    )

    // для более удобного bot.execute ( пишем params.execute() вместо params.bot.execute() )
    private fun <T : Serializable, Method : BotApiMethod<T>> Params.execute(method: Method) = bot.execute(method)

    private fun isAdminCommand(chatId: String) = chatId in adminIds

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        userInfo: UserInfo,
        bot: TelegramPollingBot,
        exp: ExpContainer,
    ) {
        val chatId = updatesUtil.getChatId(update) ?: return
        if (!isAdminCommand(chatId)) return
        val commandText = updatesUtil.getText(update)?.lowercase()?.trim() ?: return

        val params = Params(
            update,
            updatesUtil,
            userInfo,
            bot,
            exp,
            chatId,
            commandText,
        )

        when (commandText) {
            "/math_game" -> resetGlobalStageToGame(params)
            "/org_stat" -> resetGlobalStageToEndGame(params)
            "/clear_action_127" -> clearActionTable(params)
            "/clear_all_game" -> clearAllGameData(params)
            "/close_cap_reg" -> closeCapRegistration(params)
            "/close_reg" -> closeRegistration(params)
        }

        redisService.setValue(PropertyNames.STAGE_PROPERTY, exp.botGameStage.name)
        redisService.setValue(PropertyNames.STAGE_GAME_REG_PROPERTY, exp.registrationStage.name)
        redisService.setValue(PropertyNames.GLOBAL_STAGE_PROPERTY, exp.globalStage.name)
    }

    private fun resetGlobalStageToGame(params: Params) {
        actionRepository.deleteAll()
        actionRepository.deleteAllInBatch()
        teamRepository.deleteAll()
        teamRepository.deleteAllInBatch()
        params.exp.globalStage = GlobalStage.MATH_GAME
        params.exp.registrationStage = RegistrationStage.CAP_REGISTRATION
        params.exp.botGameStage = BotGameStage.REGISTRATION
        userInfoRepository.findAll().forEach {
            userInfoRepository.save(
                it.copy(
                    teamId = null,
                    isCaptain = false,
                    mcCompleted = mutableListOf(),
                ),
            )
        }

        params.execute(
            SendMessage(
                params.chatId,
                "Бот переведен в состояние игры.",
            ),
        )
    }

    private fun resetGlobalStageToEndGame(params: Params) {
        params.exp.globalStage = GlobalStage.ORGANISATION_STAGE
        teamRepository.deleteAll()
        teamRepository.deleteAllInBatch()
        params.execute(
            SendMessage(
                params.chatId,
                "Бот переведен в организационное состояние.",
            ),
        )
    }

    private fun clearActionTable(params: Params) {
        actionRepository.deleteAll()
        actionRepository.deleteAllInBatch()
        params.execute(
            SendMessage(
                params.chatId,
                "Ок, я удалил все записи из таблицы действий игры",
            ),
        )
    }

    private fun clearAllGameData(params: Params) {
        actionRepository.deleteAll()
        actionRepository.deleteAllInBatch()
        teamRepository.deleteAll()
        teamRepository.deleteAllInBatch()
        userInfoRepository.findAll().forEach {
            userInfoRepository.save(
                it.copy(
                    teamId = null,
                    isCaptain = false,
                    mcCompleted = mutableListOf(),
                ),
            )
        }
        params.execute(
            SendMessage(
                params.chatId,
                "Ок, я отчистил всю инфу от игры",
            ),
        )
    }

    private fun closeRegistration(params: Params) {
        params.exp.IS_STAGE_CHANGED = true
        params.exp.botGameStage = BotGameStage.GAME
        params.exp.registrationStage = RegistrationStage.NO_REGISTRATION
        params.execute(
            SendMessage(
                params.chatId,
                "Ок, перехожу к состоянию игры",
            ),
        )
    }

    private fun closeCapRegistration(params: Params) {
        // это (снизу) не нужно сохранять в редис
        params.exp.CAP_REGISTRATION_CLOSED_NOW = true
        params.execute(
            SendMessage(
                params.chatId,
                "Ок, закрыл регистрацию",
            ),
        )
    }
}
