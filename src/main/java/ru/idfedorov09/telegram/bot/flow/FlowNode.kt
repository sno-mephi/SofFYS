package ru.idfedorov09.telegram.bot.flow

import ru.idfedorov09.telegram.bot.fetcher.GeneralFetcher

class FlowNode(
    val fetcher: GeneralFetcher?,
    val children: MutableList<FlowNode>,
    val parents: MutableList<FlowNode>,
    val nodeType: NodeType,
) {

    fun addParentNode(parentNode: FlowNode) {
        this.parents.add(parentNode)
    }

    fun addChildrenNode(childrenNode: FlowNode): FlowNode {
        this.children.add(childrenNode)
        return childrenNode
    }

    fun addFetcher(generalFetcher: GeneralFetcher): FlowNode {
        return addChildrenNode(
            FlowNode(
                fetcher = generalFetcher,
                children = mutableListOf(),
                parents = mutableListOf(this),
                nodeType = NodeType.FETCHER,
            ),
        )
    }

    fun addWGNode(
        fetcher: GeneralFetcher? = null,
        children: MutableList<FlowNode> = mutableListOf(),
        parents: MutableList<FlowNode> = mutableListOf(this),
        nodeType: NodeType,
    ): FlowNode {
        return addChildrenNode(
            FlowNode(
                fetcher = fetcher,
                children = children,
                parents = parents,
                nodeType = nodeType,
            ),
        )
    }

    fun addWaitNode(): FlowNode {
        return addWGNode(
            nodeType = NodeType.WAIT,
        )
    }

    fun addGroupNode(): FlowNode {
        return addWGNode(
            nodeType = NodeType.GROUP,
        )
    }
}