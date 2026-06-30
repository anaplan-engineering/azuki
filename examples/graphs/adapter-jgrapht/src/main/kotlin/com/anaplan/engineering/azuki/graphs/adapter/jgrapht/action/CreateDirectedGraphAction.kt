package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

class CreateDirectedGraphAction(graphName: String) :
    CreateDirectedGraphDeclarableAction(graphName), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.addGraph(graphName, SimpleDirectedGraph<Any, DefaultEdge>(DefaultEdge::class.java))
    }
}
