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
    private val testFetcher2 = TestFetcher("2")
    private val testFetcher3 = TestFetcher("3")
    private val testFetcher4 = TestFetcher("4")
    private val testFetcher5 = TestFetcher("5")
    private val testFetcher6 = TestFetcher("6")

    private fun FlowBuilder.buildFlow() {
        group {
            fetch(testFetcher1)
            group {
                fetch(testFetcher2)
                fetch(testFetcher3)
                whenComplete {
                    fetch(testFetcher4)
                }
                fetch(testFetcher5)
                whenComplete {
                    fetch(testFetcher6)
                }
            }
        }
    }
}
