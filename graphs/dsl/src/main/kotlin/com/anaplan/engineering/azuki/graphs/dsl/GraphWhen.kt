package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.dsl.action.GraphActions

class GraphWhen(val actionFactory: GraphActionFactory): When<GraphActionFactory>,
    GraphActions
{
    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

    override fun create(graphName: String) {
        actionList.add(actionFactory.create(graphName))
    }

    override fun addVertex(graphName: String, vertex: Any) {
        actionList.add(actionFactory.addVertex(graphName,  vertex))
    }

    override fun addEdge(graphName: String, source: Any, target: Any) {
        actionList.add(actionFactory.addEdge(graphName,  source, target))
    }

}
