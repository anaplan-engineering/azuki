package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ParallelAction
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class JungActionFactory : GraphActionFactory<JungAction> {

    override fun create(graphName: String) = CreateGraphAction(graphName)

    override fun <T> addVertex(graphName: String, vertex: T) =
        AddVertexAction(graphName, vertex)

    override fun <T> addEdge(graphName: String, source: T, target: T) =
        AddEdgeAction(graphName, source, target)

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
