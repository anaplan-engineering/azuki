package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.AddEdgeBehavior
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState

abstract class AddEdgeToDirectedGraphDeclarableAction<V>(
    protected val graphName: String,
    protected val source: V,
    protected val target: V,
) : AddEdgeBehavior(), DeclarableAction<GraphDeclarationState> {

    override fun declare(state: GraphDeclarationState) {
        state.addEdgeToDirectedGraph(graphName, source, target)
    }
}
