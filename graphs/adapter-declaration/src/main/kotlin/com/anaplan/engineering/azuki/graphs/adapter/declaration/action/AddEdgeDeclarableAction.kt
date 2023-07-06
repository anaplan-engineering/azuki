package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.graphs.adapter.api.AddEdgeBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.api.AddVertexBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.api.CreateGraphBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarationBuilder

abstract class AddEdgeDeclarableAction<V>(
    protected val graphName: String,
    protected val source: V,
    protected val target: V,
): AddEdgeBehaviour(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) {
        builder.addEdge(graphName, source, target)
    }
}
