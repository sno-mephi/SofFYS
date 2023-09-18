package ru.idfedorov09.telegram.bot.flow

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.idfedorov09.telegram.bot.fetcher.MessageLogFetcher
import ru.idfedorov09.telegram.bot.fetcher.StageResolveFetcher

/**
 * Основной класс, в котором строится последовательность вычислений (граф)
 */
@Configuration
open class FlowConfiguration {

    /**
     * Возвращает построенный граф; выполняется только при запуске приложения
     */
    @Bean(name = ["flowBuilder"])
    open fun flowBuilder(): FlowBuilder {
        val flowBuilder = FlowBuilder()
        flowBuilder.buildFlow()
        return flowBuilder
    }

    private val stageResolveFetcher = StageResolveFetcher()
    private val messageLogFetcher = MessageLogFetcher()

    private fun FlowBuilder.buildFlow() {
        group {
            fetch(stageResolveFetcher)
            whenComplete {
                fetch(messageLogFetcher)
            }
        }
    }
}
