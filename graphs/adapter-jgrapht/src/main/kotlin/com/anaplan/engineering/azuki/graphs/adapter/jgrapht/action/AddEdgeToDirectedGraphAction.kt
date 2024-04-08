package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddEdgeToDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment

class AddEdgeToDirectedGraphAction<V>(graphName: String, source: V, target: V) :
    AddEdgeToDirectedGraphDeclarableAction<V>(graphName, source, target), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            if (source !in vertexSet()) {
                addVertex(source)
            }
            if (target !in vertexSet()) {
                addVertex(target)
            }
            addEdge(source, target)
        }
    }
}
