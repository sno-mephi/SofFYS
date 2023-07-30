package ru.idfedorov09.telegram.bot.entity

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.idfedorov09.telegram.bot.config.BotContainer
import java.util.concurrent.Executors

@Component
@ConditionalOnProperty(name = ["telegram.bot.interaction-method"], havingValue = "polling", matchIfMissing = true)
class TelegramPollingBot : TelegramLongPollingBot() {
    @Autowired
    private val botContainer: BotContainer? = null
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val updatingRequestDispatcher = Executors.newFixedThreadPool(Int.MAX_VALUE).asCoroutineDispatcher()

    init {
        log.info("Polling starting..")
    }

    @PostConstruct
    fun postConstruct() {
        log.info("ok, started.")
    }

    override fun onUpdateReceived(update: Update) {
        var curObj = this
        GlobalScope.launch(updatingRequestDispatcher) {
            log.info("Update received: $update")
            botContainer!!.updatesHandler.handle(curObj, update)
        }
    }

    override fun getBotUsername(): String {
        return botContainer!!.BOT_NAME
    }

    override fun getBotToken(): String {
        return botContainer!!.BOT_TOKEN
    }

    @PostConstruct
    fun botConnect() {
        var telegramBotsApi: TelegramBotsApi? = null
        try {
            telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        } catch (e: TelegramApiException) {
            log.error("Can't create API: $e")
            botConnect()
        }
        try {
            telegramBotsApi!!.registerBot(this)
            log.error("TelegramAPI started. Look for messages")
        } catch (e: TelegramApiException) {
            log.error("Can't Connect. Pause " + botContainer!!.RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.message)
            try {
                Thread.sleep(botContainer.RECONNECT_PAUSE.toLong())
            } catch (e1: InterruptedException) {
                e1.printStackTrace()
                return
            }
            botConnect()
        }
    }
}