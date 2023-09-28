package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
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
    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ) {
        val chatId = updatesUtil.getChatId(update) ?: return
        val message = updatesUtil.getText(update)?.lowercase()
        val userInfo = userInfoRepository.findByTui(chatId) ?: return
        if (message == "/mc") {
            if ((chatId != "473458128") and (chatId != "920061911")) return
            userInfoRepository.findAll().forEach { user ->
                user.tui?.let {
                    bot.execute(
                        SendMessage(
                            it,
                            "Выберите мастеркласс",
                        ).also {
                            it.replyMarkup = createChooseKeyboard()
                        },
                    )
                }
            }
        } else {
            if (!update.hasCallbackQuery()) return
            if (!update.callbackQuery.data.startsWith("mc")) return
            val mcId = update.callbackQuery.data.removePrefix("mc_")
            val mc = mcRepository.findById(mcId.toLong()).get()
            if (!checkMC(userInfo.id)) {
                bot.execute(SendMessage(chatId, "Вы уже записаны на мастеркласс!"))
                return
            }
            if (mc.users.size >= mc.maxUsersCount!!) {
                bot.execute(SendMessage(chatId, "На этом мастерклассе закончились места. Выберите другой!"))
                return
            }
            mc.users.add(userInfo.id)
            mcRepository.save(mc)
            userInfo.mcCompleted.add(userInfo.id)
            userInfoRepository.save(userInfo)
            bot.execute(SendMessage(chatId, "Вы успешно записаны на мастеркласс ${mc.name}!"))
        }
    }
    private fun createKeyboard(keyboard: List<List<InlineKeyboardButton>>) =
        InlineKeyboardMarkup().also { it.keyboard = keyboard }
    private fun createChooseKeyboard(): InlineKeyboardMarkup {
        val keyboardList = mutableListOf<List<InlineKeyboardButton>>()
        mcRepository.findAll().forEach { mc ->
            keyboardList.add(
                listOf(
                    InlineKeyboardButton("${mc.name}").also { it.callbackData = "mc_${mc.id}" },
                ),
            )
        }
        return createKeyboard(keyboardList)
    }

    private fun checkMC(id: Long): Boolean {
        mcRepository.findAll().forEach { it ->
            if (id in it.users) return false
        }
        return true
    }
}
