package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class AddEdgeAction<V>(graphName: String, source: V, target: V) :
    AddEdgeDeclarableAction<V>(graphName, source, target), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            putEdge(source, target)
        }
    }
}
