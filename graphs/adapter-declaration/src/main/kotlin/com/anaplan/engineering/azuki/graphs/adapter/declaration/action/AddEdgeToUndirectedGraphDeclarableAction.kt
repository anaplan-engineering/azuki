package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.AddEdgeBehavior
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class AddEdgeToUndirectedGraphDeclarableAction<V>(
    protected val graphName: String,
    protected val source: V,
    protected val target: V,
) : AddEdgeBehavior(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.addEdgeToUndirectedGraph(graphName, source, target)
    }
}
