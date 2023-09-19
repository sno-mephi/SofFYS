package ru.idfedorov09.telegram.bot.flow

/**
 * Экземпляры классов, помеченных этой аннотацией, могут изменяться по ходу выполнения графа
 * (без возвращения в контекст)
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Mutable
