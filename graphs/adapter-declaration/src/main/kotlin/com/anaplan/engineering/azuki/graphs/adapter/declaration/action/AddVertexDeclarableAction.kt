package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.graphs.adapter.api.AddVertexBehaviour
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class AddVertexDeclarableAction<V>(
    protected val graphName: String,
    protected val vertex: V,
): AddVertexBehaviour(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.addVertex(graphName, vertex)
    }
}
