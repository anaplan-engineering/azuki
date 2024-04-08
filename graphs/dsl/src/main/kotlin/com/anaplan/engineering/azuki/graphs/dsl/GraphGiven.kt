package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.Given
import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.dsl.action.DirectedGraphDeclarableActions
import com.anaplan.engineering.azuki.graphs.dsl.action.UndirectedGraphDeclarableActions
import com.anaplan.engineering.azuki.graphs.dsl.declaration.DirectedGraphDeclarations
import com.anaplan.engineering.azuki.graphs.dsl.declaration.UndirectedGraphDeclarations

@ScenarioDsl
class GraphGiven(private val actionFactory: GraphActionFactory<*>) : Given<GraphActionFactory<*>>,
    UndirectedGraphDeclarations, UndirectedGraphDeclarableActions, DirectedGraphDeclarations,
    DirectedGraphDeclarableActions {

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList

    override fun thereIsAnUndirectedGraph(graphName: String) {
        actionList.add(actionFactory.undirected.create(graphName))
    }

    override fun thereIsAnUndirectedGraph(graphName: String, init: UndirectedGraphBlock.() -> Unit) {
        actionList.add(actionFactory.undirected.create(graphName))
        val undirectedGraphBlock = UndirectedGraphBlock(graphName, actionFactory)
        undirectedGraphBlock.init()
        actionList.addAll(undirectedGraphBlock.actions())
    }

    override fun thereIsADirectedGraph(graphName: String) {
        actionList.add(actionFactory.directed.create(graphName))
    }

    override fun thereIsADirectedGraph(graphName: String, init: DirectedGraphBlock.() -> Unit) {
        actionList.add(actionFactory.directed.create(graphName))
        val directedGraphBlock = DirectedGraphBlock(graphName, actionFactory)
        directedGraphBlock.init()
        actionList.addAll(directedGraphBlock.actions())
    }

    override fun addVertexToUndirectedGraph(graphName: String, vertex: Any) {
        actionList.add(actionFactory.undirected.addVertex(graphName, vertex))
    }

    override fun addEdgeToUndirectedGraph(graphName: String, source: Any, target: Any) {
        actionList.add(actionFactory.undirected.addEdge(graphName, source, target))
    }

    override fun addVertexToDirectedGraph(graphName: String, vertex: Any) {
        actionList.add(actionFactory.directed.addVertex(graphName, vertex))
    }

    override fun addEdgeToDirectedGraph(graphName: String, source: Any, target: Any) {
        actionList.add(actionFactory.directed.addEdge(graphName, source, target))
    }

}
