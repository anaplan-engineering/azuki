package com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateUndirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph

class CreateUndirectedGraphAction(graphName: String) :
    CreateUndirectedGraphDeclarableAction(graphName), JGraphTAction {
    override fun act(env: ExecutionEnvironment) {
        env.addGraph(graphName, SimpleGraph<Any, DefaultEdge>(DefaultEdge::class.java))
    }

}
