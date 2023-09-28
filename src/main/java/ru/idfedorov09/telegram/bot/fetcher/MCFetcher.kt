package ru.idfedorov09.telegram.bot.fetcher

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.repo.MCRepository
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

@Component
class MCFetcher(
    private val userInfoRepository: UserInfoRepository,
    private val mcRepository: MCRepository,
) : GeneralFetcher() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
        bot: TelegramPollingBot,
        userInfo: UserInfo,
    ) {
        val chatId = updatesUtil.getChatId(update) ?: return
        val message = updatesUtil.getText(update)
        if (message?.lowercase() == "/mc") {
            if ((chatId != "473458128") and (chatId != "920061911")) return
            resetUsers()
            userInfoRepository.findAll().forEach { user ->
                user.tui?.let { tui ->
                    Thread.sleep(100L)
                    bot.execute(
                        SendMessage(
                            tui,
                            "Выбери Мастеркласс, который ты хочешь сегодня посетить",
                        ).also {
                            it.replyMarkup = createChooseKeyboard(user)
                        },
                    )
                }
            }
        } else if (message?.lowercase() == "/mc_info") {
            if ((chatId != "473458128") and (chatId != "920061911")) return
            mcRepository.findAll().forEach { mc ->
                var currentMcMessage = "Список человек, зарегистрированных на мастеркласс **${mc.name}**:"
                if (mc.users.isEmpty()) {
                    currentMcMessage = "На мастеркласс **${mc.name}** никто не зареган"
                } else {
                    var i = 1
                    mc.users.forEach {
                        val currentUser = userInfoRepository.findById(it).get()
                        currentMcMessage += "\n$i. ${currentUser.fullName}"
                        i++
                    }
                }
                bot.execute(
                    SendMessage(
                        chatId,
                        currentMcMessage,
                    ).also { it.enableMarkdown(true) },
                )
            }
        } else {
            if (!update.hasCallbackQuery()) return
            if (!update.callbackQuery.data.startsWith("mc")) return
            val mcId = update.callbackQuery.data.removePrefix("mc_")
            val mc = mcRepository.findById(mcId.toLong()).get()
            if (!userInfo.id?.let { checkMC(it) }!!) {
                bot.execute(SendMessage(chatId, "У вас слишком большие запросы! ⛔ Вы уже записаны на мастеркласс!"))
                return
            }
            if (mcId.toLong() in userInfo.mcCompleted) {
                bot.execute(SendMessage(chatId, "Ты уже проходил этот мастеркласс. Выбери другой!"))
                return
            }
            if (mc.users.size >= mc.maxUsersCount!!) {
                bot.execute(SendMessage(chatId, "На этом мастерклассе закончились места. Ты сможешь посетить его в другой день"))
                return
            }
            userInfo.id?.let { mc.users.add(it) }
            mcRepository.save(mc)
            userInfo.mcCompleted.add(mcId.toLong())
            userInfoRepository.save(userInfo)
            bot.execute(SendMessage(chatId, "Ты успешно записан на мастеркласс ${mc.name}! ✅"))
        }
    }
    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }
    private fun createChooseKeyboard(
        userInfo: UserInfo,
    ): InlineKeyboardMarkup {
        val keyboardList = mutableListOf<List<InlineKeyboardButton>>()
        mcRepository.findAll().forEach { mc ->
            if (mc.id in userInfo.mcCompleted) return@forEach
            keyboardList.add(
                listOf(
                    InlineKeyboardButton("${mc.name}").also { it.callbackData = "mc_${mc.id}" },
                ),
            )
        }
        return createKeyboard(keyboardList)
    }

    /**
     * проверяет, зарегистрирован ли пользователь на МК
     */
    private fun checkMC(id: Long): Boolean {
        mcRepository.findAll().forEach {
            if (id in it.users) return false
        }
        return true
    }

    private fun resetUsers() {
        mcRepository.findAll().forEach {
            mcRepository.save(
                it.copy(users = mutableListOf()),
            )
        }
    }
}
