package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.CreateDirectedGraphBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class CreateDirectedGraphDeclarableAction (
    protected val graphName: String
): CreateDirectedGraphBehaviour(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.declareDirectedGraph(graphName)
    }
}
