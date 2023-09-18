package ru.idfedorov09.telegram.bot.controller

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.idfedorov09.telegram.bot.UpdatesHandler
import ru.idfedorov09.telegram.bot.UpdatesSender
import ru.idfedorov09.telegram.bot.flow.FlowBuilder
import ru.idfedorov09.telegram.bot.flow.FlowContext
import ru.idfedorov09.telegram.bot.service.RedisService
import ru.idfedorov09.telegram.bot.util.UpdatesUtil
import java.util.concurrent.Executors

@Component
class UpdatesController : UpdatesSender(), UpdatesHandler {

    @Autowired
    private lateinit var flowBuilder: FlowBuilder

    @Autowired
    private lateinit var updatesUtil: UpdatesUtil

    @Autowired
    private lateinit var redisService: RedisService

    private val flowDispatcher = Executors.newFixedThreadPool(Int.MAX_VALUE).asCoroutineDispatcher()

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    private fun toContext() = listOf<Any>(
        updatesUtil,
        redisService,
    )

    // @Async("infinityThread") // if u need full async execution
    override fun handle(telegramBot: TelegramLongPollingBot, update: Update) {
        // Во время каждой прогонки графа создается свой контекст,
        // в который кладется бот и само обновление
        var flowContext = FlowContext()
        flowContext.insertObject(telegramBot)
        flowContext.insertObject(update)

        toContext().forEach { flowContext.insertObject(it) }

        val flowJob = GlobalScope.launch {
            flowBuilder.run(
                flowContext = flowContext,
            )
            // TODO: подумать, что сделать с этим; возможно, это лишнее действие
            flowContext.clear()
        }

        // ожидаем, когда граф прогонится
        runBlocking {
            flowJob.join()
        }
    }
}
