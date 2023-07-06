package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class AddEdgeAction<V>(graphName: String, source: V, target: V) :
    AddEdgeDeclarableAction<V>(graphName, source, target), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addEdge(source, target)
        }
    }
}
