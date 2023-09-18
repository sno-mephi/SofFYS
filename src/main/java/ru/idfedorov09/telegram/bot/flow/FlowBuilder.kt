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

    var exp: ExpContainer = ExpContainer()

    private var currentNode: FlowNode = FlowNode(
        GeneralFetcher(),
        mutableListOf(),
        mutableListOf(),
        NodeType.GROUP,
    )

    fun group(
        condition: (ExpContainer) -> Boolean = { true },
        action: () -> Unit,
    ) {
        val lastStateNode = currentNode
        currentNode = currentNode.addGroupNode()
        action()
        currentNode = lastStateNode
    }

    fun whenComplete(
        condition: (ExpContainer) -> Boolean = { true },
        action: () -> Unit,
    ) {
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
        exp = ExpContainer()
        // кладем эксп в контекст, чтобы была возможность менять его по ходу выполнения графа
        if (!flowContext.containsBeanByType(ExpContainer::class.java)) {
            flowContext.insertObject(ExpContainer())
        }
        coroutineScope {
            val toRun = mutableListOf<Any>()
            node.children.forEach { children ->
                // TODO: NodeType.FETCHER гарантирует ненулевой фетчер
                if (children.nodeType == NodeType.FETCHER) {
                    toRun.add(children.fetcher!!)
                } else if (children.nodeType == NodeType.GROUP) {
                    toRun.add(children)
                } else if (children.nodeType == NodeType.WAIT) {
                    val completedRun = toRun.map {
                        async {
                            when (it) {
                                is GeneralFetcher -> it.fetchMechanics(flowContext)
                                is FlowNode -> run(it, flowContext)
                            }
                        }
                    }
                    completedRun.awaitAll()
                    toRun.clear()
                    toRun.add(children)
                }
            }
            toRun.forEach {
                launch {
                    when (it) {
                        is GeneralFetcher -> it.fetchMechanics(flowContext)
                        is FlowNode -> run(it, flowContext)
                    }
                }
            }
            toRun.clear()
        }
    }
}
