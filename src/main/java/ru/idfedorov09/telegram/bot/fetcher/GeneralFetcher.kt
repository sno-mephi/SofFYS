package ru.idfedorov09.telegram.bot.fetcher

import org.apache.commons.lang3.SerializationUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.idfedorov09.telegram.bot.flow.FlowContext
import ru.idfedorov09.telegram.bot.flow.InjectData
import java.io.*
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

// TODO: нормально заинжектить flowContext
@Component
open class GeneralFetcher {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

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

                val nonCloneableObjects = mutableListOf<Any>()
                val params = mutableListOf<Any?>()
                paramTypes
                    .let { it.subList(1, it.size) }
                    .forEach { paramType ->
                        val injectedObject = flowContext.getBeanByType(paramType)
                        params.add(
                            clone(injectedObject),
                        )

                        if (injectedObject != null && !isCloneable(injectedObject)) {
                            nonCloneableObjects.add(injectedObject)
                        }
                    }

                log.warn("Flow contains non-cloneable objects: $nonCloneableObjects. Inject original instead")

                val result = method.call(this, *params.toTypedArray())
                result?.let { flowContext.insertObject(result) }
            }
        }
    }

    private fun <T : Any> clone(obj: T?): T? {
        obj ?: return null

        return when {
            obj::class.isData -> {
                val copy = obj::class.memberFunctions.firstOrNull { it.name == "copy" }
                val instanceParam = copy?.instanceParameter
                instanceParam?.let {
                    copy.callBy(mapOf(it to obj)) as T
                }
            }
            obj is Serializable -> SerializationUtils.clone(obj) as T
            else -> obj
        }
    }

    private fun isCloneable(obj: Any) = obj is Serializable || obj::class.isData
}
