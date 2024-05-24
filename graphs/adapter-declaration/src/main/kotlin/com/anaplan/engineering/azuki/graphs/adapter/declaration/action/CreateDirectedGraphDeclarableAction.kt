package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.CreateDirectedGraphBehavior
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class CreateDirectedGraphDeclarableAction(
    protected val graphName: String
) : CreateDirectedGraphBehavior(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.declareDirectedGraph(graphName)
    }
}
