package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.CreateUndirectedGraphBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class CreateUndirectedGraphDeclarableAction(
    protected val graphName: String
) : CreateUndirectedGraphBehaviour(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.declareUndirectedGraph(graphName)
    }
}
