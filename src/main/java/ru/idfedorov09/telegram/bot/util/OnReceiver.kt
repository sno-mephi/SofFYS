package ru.idfedorov09.telegram.bot.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.config.BotContainer
import java.util.concurrent.Executors

object OnReceiver {

    private val updatingRequestDispatcher = Executors.newFixedThreadPool(Int.MAX_VALUE).asCoroutineDispatcher()

    private val log = LoggerFactory.getLogger(this.javaClass)

    @JvmStatic
    fun onReceive(update: Update, botExecutor: TelegramLongPollingBot, botContainer: BotContainer?) {
        GlobalScope.launch(updatingRequestDispatcher) {
            log.info("Update received: $update")
            botContainer!!.updatesHandler.handle(botExecutor, update)
        }
    }


}
