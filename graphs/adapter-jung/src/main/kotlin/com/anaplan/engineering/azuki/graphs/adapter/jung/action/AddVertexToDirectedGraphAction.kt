package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexToDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class AddVertexToDirectedGraphAction<V>(graphName: String, vertex: V) :
    AddVertexToDirectedGraphDeclarableAction<V>(graphName, vertex), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addNode(vertex)
        }
    }
}
