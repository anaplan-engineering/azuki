package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.graphs.adapter.api.AddEdgeBehaviour
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class AddEdgeDeclarableAction<V>(
    protected val graphName: String,
    protected val source: V,
    protected val target: V,
): AddEdgeBehaviour(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.addEdge(graphName, source, target)
    }
}
