package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.CreateWorldBehaviour
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState

abstract class CreateWorldDeclarableAction(
    protected val worldName: String,
) : CreateWorldBehaviour(), DeclarableAction<IntraWorldDeclarationState> {

    override fun declare(state: IntraWorldDeclarationState) = state.declareWorld(worldName)
}
