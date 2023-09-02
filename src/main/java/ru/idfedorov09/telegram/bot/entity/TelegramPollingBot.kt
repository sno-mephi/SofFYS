package ru.idfedorov09.telegram.bot.entity

import jakarta.annotation.PostConstruct
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
import ru.idfedorov09.telegram.bot.util.OnReceiver

@Component
@ConditionalOnProperty(name = ["telegram.bot.interaction-method"], havingValue = "polling", matchIfMissing = true)
class TelegramPollingBot : TelegramLongPollingBot() {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    @Autowired
    private lateinit var botContainer: BotContainer

    @Autowired
    private lateinit var updateReceiver: OnReceiver

    init {
        log.info("Polling starting..")
    }

    @PostConstruct
    fun postConstruct() {
        log.info("ok, started.")
    }

    override fun onUpdateReceived(update: Update) {
        updateReceiver.onReceive(update, this)
    }

    override fun getBotUsername(): String {
        return botContainer.BOT_NAME
    }

    override fun getBotToken(): String {
        return botContainer.BOT_TOKEN
    }

    @PostConstruct
    fun botConnect() {
        lateinit var telegramBotsApi: TelegramBotsApi

        try {
            telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        } catch (e: TelegramApiException) {
            log.error("Can't create API: $e. Trying to reconnect..")
            botConnect()
        }

        try {
            telegramBotsApi.registerBot(this)
            log.info("TelegramAPI started. Look for messages")
        } catch (e: TelegramApiException) {
            log.error("Can't Connect. Pause " + botContainer.RECONNECT_PAUSE / 1000 + "sec and try again. Error: " + e.message)
            try {
                Thread.sleep(botContainer.RECONNECT_PAUSE.toLong())
            } catch (threadError: InterruptedException) {
                log.error(threadError.message)
                return
            }
            botConnect()
        }
    }
}
