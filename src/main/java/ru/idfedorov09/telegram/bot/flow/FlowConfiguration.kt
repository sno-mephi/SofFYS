package ru.idfedorov09.telegram.bot.flow

import ru.idfedorov09.telegram.bot.fetcher.TestFetcher

/**
 * Основной класс, в котором строиться последовательность вычислений (граф)
 */
class FlowConfiguration {

    companion object {
        val testFetcher = TestFetcher()
    }

    fun FlowBuilder.buildFlow() {
        group {
            fetch(testFetcher)
        }
    }
}
