package ru.idfedorov09.telegram.bot.flow

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher

class FlowBuilder {

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }

    private var currentNode: FlowNode = FlowNode(
        GeneralFetcher(),
        mutableListOf(),
        mutableListOf(),
        NodeType.GROUP,
    )

    fun group(action: () -> Unit) {
        val lastStateNode = currentNode
        currentNode = currentNode.addGroupNode()
        action()
        currentNode = lastStateNode
    }

    fun whenComplete(action: () -> Unit) {
        val lastStateNode = currentNode
        currentNode = currentNode.addWaitNode()
        action()
        currentNode = lastStateNode
    }

    fun fetch(fetcherInstance: GeneralFetcher) {
        // fetcherInstance.fetchMechanics() // вызывает метод фетчера, помеченный аннотацией @InjectData
        // вызываться должно после построения графа!
        currentNode.addFetcher(fetcherInstance)
    }

    suspend fun run(
        node: FlowNode = currentNode,
        flowContext: FlowContext,
    ) {
        coroutineScope {
            val toRun = mutableListOf<GeneralFetcher>()
            node.children.forEach { children ->
                // TODO: NodeType.FETCHER гарантирует ненулевой фетчер
                if (children.nodeType == NodeType.FETCHER) {
                    toRun.add(children.fetcher!!)
                } else if (children.nodeType == NodeType.WAIT) {
                    val completedRun = toRun.map { async { it.fetchMechanics(flowContext) } }
                    completedRun.awaitAll()
                    toRun.clear()
                    launch { run(children, flowContext) }
                } else {
                    launch { run(children, flowContext) }
                }
            }
            toRun.forEach {
                launch { it.fetchMechanics(flowContext) }
            }
            toRun.clear()
        }
    }
}
