package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexToUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class AddVertexToUndirectedGraphAction<V>(graphName: String, vertex: V) :
    AddVertexToUndirectedGraphDeclarableAction<V>(graphName, vertex), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addVertex(vertex)
        }
    }


}
