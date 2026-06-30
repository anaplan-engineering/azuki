package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeToUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class AddEdgeToUndirectedGraphAction<V>(graphName: String, source: V, target: V) :
    AddEdgeToUndirectedGraphDeclarableAction<V>(graphName, source, target), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addEdge(source, target, Pair(source, target))
        }
    }
}
