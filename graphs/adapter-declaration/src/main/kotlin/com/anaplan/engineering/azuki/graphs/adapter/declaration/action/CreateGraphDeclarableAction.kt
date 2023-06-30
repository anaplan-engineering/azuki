package com.anaplan.engineering.azuki.graphs.adapter.declaration.action

import com.anaplan.engineering.azuki.graphs.adapter.api.CreateGraphBehaviour
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarationBuilder

abstract class CreateGraphDeclarableAction(
    protected val graphName: String
): CreateGraphBehaviour(), DeclarableAction {

    override fun declare(builder: DeclarationBuilder) {
        builder.declareGraph(graphName)
    }
}
