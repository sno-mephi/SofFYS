package ru.idfedorov09.telegram.bot.flow

import org.springframework.stereotype.Component

@Component
class FlowContext {

    private val contextMap: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Добавляет в контекст объект, если он не нулевой
     */
    fun insertObject(obj: Any?) {
        obj?.let {
            val objClass = obj.javaClass
            contextMap[objClass.name] = obj
        }
    }

    /**
     * Извлекает из контекста объект по типу, если такой есть
     */
    // TODO: обработка исключения при ненайденном типе
    fun getBeanByType(clazz: Class<*>): Any? {
        return contextMap[clazz.name]
    }

    /**
     * Очищает контекст
     */
    fun clear() {
        contextMap.clear()
    }
}
