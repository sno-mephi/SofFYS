package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.PropertyNames
import ru.idfedorov09.telegram.bot.data.enums.RegistrationStage
import ru.idfedorov09.telegram.bot.data.enums.UserResponseType
import ru.idfedorov09.telegram.bot.data.model.Team
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.model.UserResponse
import ru.idfedorov09.telegram.bot.data.repo.TeamRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.service.RedisService
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.jvm.optionals.getOrNull

@Component
class RegFetcher(
    private val teamRepository: TeamRepository,
    private val userInfoRepository: UserInfoRepository,
    private val redisService: RedisService,
) : GeneralFetcher() {
    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
        private val confirmPrefix = "team_confirm "
    }

    @InjectData
    fun doFetch(
        userResponse: UserResponse,
        bot: TelegramPollingBot,
        exp: ExpContainer,
        update: Update,
        userInfo: UserInfo,
    ) {
        val tui = userResponse.initiator.tui ?: return

        when (exp.registrationStage) {
            RegistrationStage.CAP_REGISTRATION -> {
                when (userResponse.userResponseType) {
                    UserResponseType.MESSAGE_RESPONSE -> {
                        if (userResponse.initiator.isCaptain) {
                            bot.execute(SendMessage(tui, "Ты уже создал команду"))
                            return
                        }
                        selectTeamName(tui, userResponse, bot)
                    }

                    UserResponseType.BUTTON_RESPONSE -> {
                        confirmTeamCreate(tui, update, bot, userInfo)
                    }
                    else -> return
                }
            }

            RegistrationStage.TEAM_REGISTRATION -> {
                if (userResponse.userResponseType != UserResponseType.BUTTON_RESPONSE) {
                    return
                }
                messageFromNoCap(tui, update, bot, userInfo)
            }

            else -> return
        }
    }

    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }

    private fun createChooseKeyboard(
        teamName: String?,
    ) = createKeyboard(
        listOf(
            listOf(
                InlineKeyboardButton("Да ✅").also { it.callbackData = "team_confirm $teamName" },
                InlineKeyboardButton("Нет ❌").also { it.callbackData = "team_reg_cancel" },
            ),
        ),
    )

    private fun selectTeamName(
        tui: String,
        userResponse: UserResponse,
        bot: TelegramPollingBot,
    ) {
        val teamName = userResponse.messageText?.trim() ?: return

        teamRepository.findAll()
            .forEach { team ->
                if (team.teamName?.lowercase() == teamName.lowercase()) {
                    bot.execute(SendMessage(tui, "Команда с таким названием уже существует!!!"))
                    return
                }
            }

        bot.execute(
            SendMessage(
                tui,
                "Вы точно хотите зарегистрировать команду *$teamName*?",
            ).also {
                it.enableMarkdown(true)
                it.replyMarkup = createChooseKeyboard(teamName)
            },
        )
    }

    private fun confirmTeamCreate(
        tui: String,
        update: Update,
        bot: TelegramPollingBot,
        userInfo: UserInfo,
    ) {
        val answer = update.callbackQuery.data
        if (answer.startsWith(confirmPrefix)) {
            if (userInfo.isCaptain) {
                bot.execute(
                    SendMessage(
                        tui,
                        "Ты уже зарегал команду",
                    ),
                )
                return
            }
            val teamName = answer.removePrefix(confirmPrefix)

            val savedTeam: Team =
                teamRepository.save(
                    Team(
                        teamName = teamName,
                        lastBoardHash = redisService.getSafe(PropertyNames.START_BOARD_HASH),
                    ),
                )
            userInfoRepository.save(
                userInfo.copy(
                    isCaptain = true,
                    teamId = savedTeam.id,
                ),
            )

            bot.execute(
                SendMessage(
                    tui,
                    "Ура, твоя команда *$teamName* зарегистрирована",
                ).also {
                    it.enableMarkdown(true)
                },
            )

            copyFile("images/templates/initial.png", "images/boards/${savedTeam.id}.png")
        } else if (answer == "team_reg_cancel") {
            if (userInfo.isCaptain) {
                bot.execute(
                    SendMessage(
                        tui,
                        "ты уже создал команду!",
                    ),
                )
            } else {
                bot.execute(
                    DeleteMessage(
                        tui,
                        update.callbackQuery.message.messageId.toInt(),
                    ),
                )
            }
        }
    }

    private fun messageFromNoCap(
        tui: String,
        update: Update,
        bot: TelegramPollingBot,
        userInfo: UserInfo,
    ) {
        val teamId = update.callbackQuery.data.toLongOrNull()
        val team = teamId?.let { teamRepository.findById(it).getOrNull() }
        userInfoRepository.save(
            userInfo.copy(
                teamId = teamId,
            ),
        )
        bot.execute(
            SendMessage(
                tui,
                "Ты успешно вступил в команду *`${team?.teamName}`*!",
            ).also {
                it.enableMarkdown(true)
            },
        )
    }

    fun copyFile(source: String, destination: String) {
        val sourcePath = Path.of(source)
        val destinationPath = Path.of(destination)
        try {
            Files.createDirectories(destinationPath.parent)
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
            log.info("Create command board: $sourcePath -> $destinationPath")
        } catch (e: IOException) {
            log.error("can't create command board: $e")
        }
    }
}
