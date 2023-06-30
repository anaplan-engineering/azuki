package com.anaplan.engineering.azuki.graphs.adapter.jung.action

import com.anaplan.engineering.azuki.graphs.adapter.declaration.action.CreateGraphDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import com.google.common.graph.GraphBuilder

class CreateGraphAction(graphName: String) :
    CreateGraphDeclarableAction(graphName), JungAction {
    override fun act(env: ExecutionEnvironment) {
        env.addGraph(graphName, GraphBuilder.undirected().build<Any>())
    }

}
