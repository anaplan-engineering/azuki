package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.dsl.action.GraphDeclarableActions
import com.anaplan.engineering.azuki.graphs.dsl.declaration.GraphDeclarations

@ScenarioDsl
class GraphGiven(private val actionFactory: GraphActionFactory): Given<GraphActionFactory>,
    GraphDeclarations, GraphDeclarableActions
{

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList

    override fun thereIsAGraph(graphName: String) {
        actionList.add(actionFactory.create(graphName))
    }

    override fun thereIsAGraph(graphName: String, init: GraphBlock.() -> Unit) {
        actionList.add(actionFactory.create(graphName))
        val graphBlock = GraphBlock(graphName, actionFactory)
        graphBlock.init()
        actionList.addAll(graphBlock.actions())
    }

    override fun addVertex(graphName: String, vertex: Any) {
        actionList.add(actionFactory.addVertex(graphName,  vertex))
    }

    override fun addEdge(graphName: String, source: Any, target: Any) {
        actionList.add(actionFactory.addEdge(graphName,  source, target))
    }

}
