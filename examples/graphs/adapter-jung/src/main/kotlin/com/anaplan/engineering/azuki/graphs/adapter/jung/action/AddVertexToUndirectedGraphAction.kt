package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexToUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class AddVertexToUndirectedGraphAction<V>(graphName: String, vertex: V) :
    AddVertexToUndirectedGraphDeclarableAction<V>(graphName, vertex), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addNode(vertex)
        }
    }
}
