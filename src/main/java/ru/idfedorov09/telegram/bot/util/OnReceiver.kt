package ru.idfedorov09.telegram.bot.util

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import redis.clients.jedis.Jedis
import ru.idfedorov09.telegram.bot.config.BotContainer
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.service.UserQueue
import java.util.concurrent.Executors

@Component
class OnReceiver {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @Autowired
    private lateinit var botContainer: BotContainer

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var updatesUtil: UpdatesUtil

    @Autowired
    private lateinit var userQueue: UserQueue

    private val updatingRequestDispatcher = Executors.newFixedThreadPool(Int.MAX_VALUE).asCoroutineDispatcher()

    private fun execOne(update: Update, executor: TelegramLongPollingBot?) {
        log.info("Update received: $update")
        botContainer.updatesHandler.handle(executor, update)
    }

    private fun exec(update: Update, executor: TelegramLongPollingBot?) {
        val chatId = updatesUtil.getChatId(update)

        if (chatId == null) {
            execOne(update, executor)
            return
        }

        val chatKey = updatesUtil.getChatKey(chatId)

        if (redisService.getSafe(chatKey) == null) {
            redisService.setValue(chatKey, "1")
            execOne(update, executor)
            redisService.del(chatKey)

            val upd: Update? = userQueue.popUpdate(chatId)
            upd?.let { onReceive(upd, executor) }
        } else {
            userQueue.push(update, chatId)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onReceive(update: Update, executor: TelegramLongPollingBot?) {
        GlobalScope.launch(updatingRequestDispatcher) {
            exec(update, executor)
        }
    }
}
