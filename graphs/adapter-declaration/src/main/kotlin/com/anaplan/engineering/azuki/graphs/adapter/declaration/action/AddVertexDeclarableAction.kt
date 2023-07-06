package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.graphs.adapter.api.AddVertexBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.api.CreateGraphBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarationBuilder

abstract class AddVertexDeclarableAction<V>(
    protected val graphName: String,
    protected val vertex: V,
): AddVertexBehaviour(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) {
        builder.addVertex(graphName, vertex)
    }
}
