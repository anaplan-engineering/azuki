package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory

@ScenarioDsl
class GraphGiven(private val actionFactory: GraphActionFactory): Given<GraphActionFactory> {

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList

    fun thereIsAGraph(graphName: String) {
        actionList.add(actionFactory.create(graphName))
    }

    fun thereIsAGraph(graphName: String, init: GraphBlock.() -> Unit) {
        actionList.add(actionFactory.create(graphName))
        val graphBlock = GraphBlock(graphName, actionFactory)
        graphBlock.init()
        actionList.addAll(graphBlock.actions())
    }

}
