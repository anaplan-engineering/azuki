package com.anaplan.engineering.azuki.graphs.adapter.declaration

import com.anaplan.engineering.azuki.core.system.Action

interface DeclarableAction : Action {
    fun declare(builder: DeclarationBuilder)
}

val toDeclarableAction: (Action) -> DeclarableAction = {
    it as? DeclarableAction ?: throw IllegalArgumentException("Invalid action: $it")
}
