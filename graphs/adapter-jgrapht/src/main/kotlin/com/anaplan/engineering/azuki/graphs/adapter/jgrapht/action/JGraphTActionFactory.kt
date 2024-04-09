package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class JGraphTActionFactory : GraphActionFactory<JGraphTAction> {

    // TODO - directed etc
    override fun create(graphName: String) = CreateGraphAction(graphName)

    override fun <T> addVertex(graphName: String, vertex: T) =
        AddVertexAction(graphName, vertex)

    override fun <T> addEdge(graphName: String, source: T, target: T) =
        AddEdgeAction(graphName, source, target)

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
