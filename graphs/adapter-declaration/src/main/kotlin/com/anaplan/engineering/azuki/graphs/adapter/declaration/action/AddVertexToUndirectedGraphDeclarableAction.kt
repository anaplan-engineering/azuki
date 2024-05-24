package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.AddVertexBehavior
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class AddVertexToUndirectedGraphDeclarableAction<V>(
    protected val graphName: String,
    protected val vertex: V,
) : AddVertexBehavior(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.addVertexToUndirectedGraph(graphName, vertex)
    }
}
