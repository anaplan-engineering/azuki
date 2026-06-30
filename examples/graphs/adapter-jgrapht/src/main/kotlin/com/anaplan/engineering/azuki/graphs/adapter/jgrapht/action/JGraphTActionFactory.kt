package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction
import com.anaplan.engineering.azuki.graphs.adapter.api.DirectedGraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.UndirectedGraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class JGraphTActionFactory : GraphActionFactory<JGraphTAction> {

    override val undirected: UndirectedGraphActionFactory = JGraphTUndirectedGraphActionFactory

    override val directed: DirectedGraphActionFactory = JGraphTDirectedGraphActionFactory

    override fun createParallelAction(actions: List<List<Action>>) =
        JGraphTParallelAction(actions.map { it.map(toJGraphTAction) })
}


interface JGraphTAction : Action {

    fun act(env: ExecutionEnvironment)

}

class JGraphTParallelAction(actions: List<List<JGraphTAction>>) : ParallelAction<JGraphTAction>(actions),
    JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        runActionsConcurrently { action -> action.act(env) }
    }
}


val toJGraphTAction: (Action) -> JGraphTAction = {
    @Suppress("UNCHECKED_CAST")
    it as? JGraphTAction ?: throw IllegalArgumentException("Invalid action: $it")
}

private object JGraphTUndirectedGraphActionFactory : UndirectedGraphActionFactory {

    override fun create(graphName: String) = CreateUndirectedGraphAction(graphName)

    override fun <T> addVertex(graphName: String, vertex: T) =
        AddVertexToUndirectedGraphAction(graphName, vertex)

    override fun <T> addEdge(graphName: String, source: T, target: T) =
        AddEdgeToUndirectedGraphAction(graphName, source, target)

}

private object JGraphTDirectedGraphActionFactory : DirectedGraphActionFactory {

    override fun create(graphName: String) = CreateDirectedGraphAction(graphName)

    override fun <T> addVertex(graphName: String, vertex: T) =
        AddVertexToDirectedGraphAction(graphName, vertex)

    override fun <T> addEdge(graphName: String, source: T, target: T) =
        AddEdgeToDirectedGraphAction(graphName, source, target)


}
