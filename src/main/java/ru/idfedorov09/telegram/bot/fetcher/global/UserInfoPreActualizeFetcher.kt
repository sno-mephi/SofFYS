package ru.idfedorov09.telegram.bot.fetcher.global

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.data.model.UserInfo
import ru.idfedorov09.telegram.bot.data.repo.UserInfoRepository
import ru.idfedorov09.telegram.bot.executor.TelegramPollingBot
import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher
import ru.idfedorov09.telegram.bot.flow.ExpContainer
import ru.idfedorov09.telegram.bot.flow.InjectData
import ru.idfedorov09.telegram.bot.util.UpdatesUtil

@Component
class UserInfoPreActualizeFetcher(
    private val userInfoRepository: UserInfoRepository,
) : GeneralFetcher() {

    @InjectData
    fun doFetch(
        update: Update,
        updatesUtil: UpdatesUtil,
        exp: ExpContainer,
        bot: TelegramPollingBot,
    ): UserInfo? {
        val chatId = updatesUtil.getChatId(update) ?: run {
            exp.hasChatId = false
            return null
        }
        exp.hasChatId = true

        if (update.hasCallbackQuery()) {
            bot.execute(
                AnswerCallbackQuery().also {
                    it.callbackQueryId = update.callbackQuery.id
                },
            )
        }

        return userInfoRepository.findByTui(chatId)
            ?: UserInfo(tui = chatId).also { userInfoRepository.save(it) }
    }
}
