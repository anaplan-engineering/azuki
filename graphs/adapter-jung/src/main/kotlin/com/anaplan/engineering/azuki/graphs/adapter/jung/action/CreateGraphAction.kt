package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.GraphBuilder
import com.google.common.graph.NetworkBuilder

class CreateGraphAction(graphName: String) :
    CreateGraphDeclarableAction(graphName), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.addGraph(graphName, NetworkBuilder.undirected().build<Any, Pair<*, *>>())
    }

}
