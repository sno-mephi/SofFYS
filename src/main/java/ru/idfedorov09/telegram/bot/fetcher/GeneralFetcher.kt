package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.stereotype.Component
import ru.idfedorov09.telegram.bot.flow.FlowContext
import ru.idfedorov09.telegram.bot.flow.InjectData
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaType

// TODO: нормально заинжектить flowContext
@Component
open class GeneralFetcher {

    /**
     * Метод, который запускает метод помеченный как @InjectData,
     * внедряя в него нужные бины из контекста
     */
    fun fetchMechanics(flowContext: FlowContext) {
        val methods = this::class.declaredMemberFunctions

        // TODO: обработать случай когда методов несколько (ошибка)
        for (method in methods) {
            if (method.hasAnnotation<InjectData>()) {
                val paramTypes = method.parameters
                    .map { it.type.javaType as Class<*> }

                val params = mutableListOf<Any?>()
                paramTypes
                    .let { it.subList(1, it.size) }
                    .forEach { paramType ->
                        params.add(
                            flowContext.getBeanByType(paramType),
                        )
                    }

                val result = method.call(this, *params.toTypedArray())

                result?.let { flowContext.insertObject(result) }
            }
        }
    }
}
