package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment

class AddVertexAction<V>(graphName: String, vertex: V) :
    AddVertexDeclarableAction<V>(graphName, vertex), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addNode(vertex)
        }
    }
}
