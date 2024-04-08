package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateDirectedGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.NetworkBuilder

class CreateDirectedGraphAction(graphName: String) :
    CreateDirectedGraphDeclarableAction(graphName), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.addGraph(graphName, NetworkBuilder.directed().build<Any, Pair<*, *>>())
    }
}
