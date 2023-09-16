package ru.idfedorov09.telegram.bot.flow

import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher

class FlowBuilder {
    var currentNode: FlowNode? = FlowNode(
        GeneralFetcher(),
        mutableListOf(),
        mutableListOf(),
        NodeType.GROUP,
    )

    fun group(action: () -> Unit) {
        val lastStateNode = currentNode
        currentNode = currentNode?.addGroupNode()
        action()
        currentNode = lastStateNode
    }

    fun whenComplete(action: () -> Unit) {
        val lastStateNode = currentNode
        currentNode = currentNode?.addWaitNode()
        action()
        currentNode = lastStateNode
    }

    fun fetch(fetcherInstance: GeneralFetcher) {
        fetcherInstance.fetchMechanics() // вызывает метод фетчера, помеченный аннотацией @InjectData
        currentNode?.addFetcher(fetcherInstance)
        println()
    }
}
