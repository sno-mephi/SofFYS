package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService

@Component
class UserRegFetcher(
    private val redisService: RedisService,
    private val teamRepository: TeamRepository,
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
        update: Update,
    ) {
        if (!exp.CAP_REGISTRATION_CLOSED_NOW) {
            return
        }

        exp.registrationStage = RegistrationStage.TEAM_REGISTRATION
        redisService.setValue(PropertyNames.STAGE_GAME_REG_PROPERTY, exp.registrationStage.name)

        userInfoRepository.findAll()
            .forEach { user ->
                val tui = user.tui ?: return
                if (!user.isCaptain) {
                    bot.execute(
                        SendMessage(
                            tui,
                            "Выберите одну из команд:",
                        ).also {
                            it.replyMarkup = createChooseKeyboard()
                        },
                    )
                }
            }
    }

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }

    private fun createChooseKeyboard(): InlineKeyboardMarkup {
        val keyboardList = mutableListOf<List<InlineKeyboardButton>>()
        teamRepository.findAll().forEach { team ->
            keyboardList.add(
                listOf(
                    InlineKeyboardButton("${team.teamName}").also { it.callbackData = "${team.id}" },
                ),
            )
        }
        return createKeyboard(keyboardList)
    }
}
