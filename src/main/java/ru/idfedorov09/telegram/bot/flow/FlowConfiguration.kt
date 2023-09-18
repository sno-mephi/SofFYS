package ru.idfedorov09.telegram.bot.flow

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.idfedorov09.telegram.bot.fetcher.TestFetcher

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

    private val testFetcher1 = TestFetcher("1")
    private val testFetcherStart = TestFetcher("START")

    private fun FlowBuilder.buildFlow() {
        group(condition = { exp.isAppeal }) {
            fetch(testFetcherStart)
            whenComplete { fetch(testFetcher1) }
        }
    }
}
