package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeToDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class AddEdgeToDirectedGraphAction<V>(graphName: String, source: V, target: V) :
    AddEdgeToDirectedGraphDeclarableAction<V>(graphName, source, target), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addEdge(source, target, Pair(source, target))
        }
    }
}
