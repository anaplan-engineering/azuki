package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.AddVertexDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph

class AddVertexAction<V>(graphName: String, vertex: V) :
    AddVertexDeclarableAction<V>(graphName, vertex), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.act(graphName) {
            addVertex(vertex)
        }
    }


}
