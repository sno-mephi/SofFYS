package ru.idfedorov09.telegram.bot.flow

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.idfedorov09.telegram.bot.data.enums.BotStage
import ru.idfedorov09.telegram.bot.fetcher.AdminCommandsFetcher
import ru.idfedorov09.telegram.bot.fetcher.CommandValidateResponseFetcher
import ru.idfedorov09.telegram.bot.fetcher.StageResolveFetcher
import ru.idfedorov09.telegram.bot.fetcher.StateFetcher

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

    @Autowired
    private lateinit var stageResolveFetcher: StageResolveFetcher
    @Autowired
    private lateinit var stateFetcher: StateFetcher
    @Autowired
    private lateinit var commandValidateResponseFetcher: CommandValidateResponseFetcher

    @Autowired
    private lateinit var adminCommandsFetcher: AdminCommandsFetcher

    // TODO: идея по автоматической смене состояния по просшествия определенного времени
    // самым первым фетчером сделать фетчер, который отвечает за смену состояния - при нулевом update
    // и просто запускать граф в отложенных задачах!

    // TODO: при изменении botStage добавить моментальное изменение его в редисе!
    private fun FlowBuilder.buildFlow() {
        group {
            fetch(stageResolveFetcher)
            fetch(commandValidateResponseFetcher)
            // если пришедшая команда валидная, то работаем дальше
            whenComplete(condition = { exp.IS_VALID_COMMAND }) {
                fetch(adminCommandsFetcher)
                whenComplete {
                    group(condition = { exp.botStage == BotStage.OFFLINE }) {
                    }
                    group(condition = { exp.botStage == BotStage.REGISTRATION }) {
                    }
                    group(condition = { exp.botStage == BotStage.GAME }) {
                    }
                    group(condition = { exp.botStage == BotStage.APPEAL }) {
                    }
                    group(condition = { exp.botStage == BotStage.AFTER_APPEAL }) {
                    }
                }
                fetch(stateFetcher)
            }
        }
    }
}
