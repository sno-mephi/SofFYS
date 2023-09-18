package ru.idfedorov09.telegram.bot.flow

import org.slf4j.LoggerFactory

data class FlowContext(
    private val contextMap: MutableMap<String, Any?> = mutableMapOf()
) {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

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
     * Извлекает из контекста объект по типу, если такой есть; если нет то возвращается null
     */
    open fun getBeanByType(clazz: Class<*>): Any? {
        if (!containsBeanByType(clazz)) {
            log.warn("Context doesn't contains object with type ${clazz.name}")
            return null
        }
        return contextMap[clazz.name]
    }

    fun containsBeanByType(clazz: Class<*>): Boolean {
        return contextMap.contains(clazz.name)
    }

    /**
     * Очищает контекст
     */
    fun clear() {
        contextMap.clear()
    }
}
