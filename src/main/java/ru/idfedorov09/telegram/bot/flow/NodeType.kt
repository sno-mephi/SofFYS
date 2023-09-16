package ru.idfedorov09.telegram.bot.flow

// WGNode - узел типа WAIT/GROUP
enum class NodeType {
    WAIT, // узел ожидания (ожидает завершения выполнения всех предков); fetcher = null
    GROUP, // типа WAIT, но без ожидания - просто для объединения в группу; fetcher = null
    FETCHER, // выполняющийся узел; fetcher!=null
}
