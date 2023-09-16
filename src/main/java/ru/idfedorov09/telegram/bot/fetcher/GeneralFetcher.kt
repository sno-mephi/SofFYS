package ru.idfedorov09.telegram.bot.fetcher

import org.springframework.beans.factory.annotation.Autowired
import ru.idfedorov09.telegram.bot.flow.FlowContext
import ru.idfedorov09.telegram.bot.flow.InjectData
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaType

open class GeneralFetcher {

    @Autowired
    private lateinit var flowContext: FlowContext

    /**
     * Метод, который запускает метод помеченный как @InjectData,
     * внедряя в него нужные бины из контекста
     */
    fun fetchMechanics() {
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
