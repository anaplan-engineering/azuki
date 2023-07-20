package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.graphs.adapter.api.CreateGraphBehaviour
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class CreateGraphDeclarableAction(
    protected val graphName: String
): CreateGraphBehaviour(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.declareGraph(graphName)
    }
}
