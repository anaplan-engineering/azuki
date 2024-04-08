package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction
import com.anaplan.engineering.azuki.graphs.adapter.api.DirectedGraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.UndirectedGraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class JungActionFactory : GraphActionFactory<JungAction> {

    override val undirected: UndirectedGraphActionFactory = JungUndirectedGraphActionFactory

    override val directed: DirectedGraphActionFactory = JungDirectedGraphActionFactory

    override fun createParallelAction(actions: List<List<Action>>) =
        JungParallelAction(actions.map { it.map(toJungAction) })
}

interface JungAction : Action {

    fun act(env: ExecutionEnvironment)

}

class JungParallelAction(actions: List<List<JungAction>>) : ParallelAction<JungAction>(actions), JungAction {
    override fun act(env: ExecutionEnvironment) {
        runActionsConcurrently { action -> action.act(env) }
    }
}

val toJungAction: (Action) -> JungAction = {
    @Suppress("UNCHECKED_CAST")
    it as? JungAction ?: throw IllegalArgumentException("Invalid action: $it")
}

private object JungUndirectedGraphActionFactory : UndirectedGraphActionFactory {

    override fun create(graphName: String) = CreateUndirectedGraphAction(graphName)

    override fun <T> addVertex(graphName: String, vertex: T) =
        AddVertexToUndirectedGraphAction(graphName, vertex)

    override fun <T> addEdge(graphName: String, source: T, target: T) =
        AddEdgeToUndirectedGraphAction(graphName, source, target)

}

private object JungDirectedGraphActionFactory : DirectedGraphActionFactory {

    override fun create(graphName: String) = CreateDirectedGraphAction(graphName)

    override fun <T> addVertex(graphName: String, vertex: T) =
        AddVertexToDirectedGraphAction(graphName, vertex)

    override fun <T> addEdge(graphName: String, source: T, target: T) =
        AddEdgeToDirectedGraphAction(graphName, source, target)

}
