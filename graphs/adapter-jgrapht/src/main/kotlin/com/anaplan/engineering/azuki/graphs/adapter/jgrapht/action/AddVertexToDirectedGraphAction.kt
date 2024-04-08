package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexToDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class AddVertexToDirectedGraphAction<V>(graphName: String, vertex: V) :
    AddVertexToDirectedGraphDeclarableAction<V>(graphName, vertex), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addVertex(vertex)
        }
    }


}
