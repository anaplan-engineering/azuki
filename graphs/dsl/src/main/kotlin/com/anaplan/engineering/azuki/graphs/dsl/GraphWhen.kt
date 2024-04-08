package com.anaplan.engineering.azuki.graphs.dsl

import com.anaplan.engineering.azuki.core.dsl.ParallelWhen
import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.dsl.action.DirectedGraphActions
import com.anaplan.engineering.azuki.graphs.dsl.action.UndirectedGraphActions

class GraphWhen(private val actionFactory: GraphActionFactory<*>) :
    When<GraphActionFactory<*>>,
    ParallelWhen<GraphActionFactory<*>, GraphWhen>,
    UndirectedGraphActions,
    DirectedGraphActions {
    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

    override fun createUndirected(graphName: String) {
        actionList.add(actionFactory.undirected.create(graphName))
    }

    override fun createDirected(graphName: String) {
        actionList.add(actionFactory.directed.create(graphName))
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


    override fun parallel(vararg fns: GraphWhen.() -> Unit) {
        actionList.add(actionFactory.createParallelAction(
            fns.map { fn ->
                val w = GraphWhen(actionFactory)
                w.fn()
                w.actions()
            }
        ))
    }

}
