package ru.idfedorov09.telegram.bot.flow

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.idfedorov09.telegram.bot.data.enums.BotStage
import ru.idfedorov09.telegram.bot.fetcher.*

/**
 * Основной класс, в котором строится последовательность вычислений (граф)
 */
@Configuration
open class FlowConfiguration(
    private val stageResolveFetcher: StageResolveFetcher,
    private val commandValidateResponseFetcher: CommandValidateResponseFetcher,
    private val stateFetcher: StateFetcher,
    private val topFetcher: TopFetcher,
    private val adminCommandsFetcher: AdminCommandsFetcher,
    private val regFetcher: RegFetcher,
    private val newProblemFetcher: NewProblemFetcher,
    private val answerFetcher: AnswerFetcher,
    private val userRegFetcher: UserRegFetcher,
    private val poolFetcher: PoolFetcher,
    private val apealFetcher: ApealFetcher,
    private val adminComfirmApealFetcher: AdminComfirmApealFetcher,
    private val actionFetcher: ActionFetcher,
) {

    /**
     * Возвращает построенный граф; выполняется только при запуске приложения
     */
    @Bean(name = ["flowBuilder"])
    open fun flowBuilder(): FlowBuilder {
        val flowBuilder = FlowBuilder()
        flowBuilder.buildFlow()
        return flowBuilder
    }

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
                        fetch(regFetcher)
                        fetch(userRegFetcher)
                    }
                    group(condition = { exp.botStage == BotStage.GAME }) {
                        fetch(topFetcher)
                        fetch(poolFetcher)
                        fetch(newProblemFetcher)
                        fetch(answerFetcher)
                    }
                    group(condition = { exp.botStage == BotStage.APPEAL }) {
                        fetch(apealFetcher)
                        fetch(adminComfirmApealFetcher)
                        fetch(topFetcher)
                    }
                    group(condition = { exp.botStage == BotStage.AFTER_APPEAL }) {
                        fetch(topFetcher)
                        fetch(adminComfirmApealFetcher)
                    }
                    fetch(stateFetcher)
                }
              whenComplete { fetch(actionFetcher) } 
            }
        }
    }
}
