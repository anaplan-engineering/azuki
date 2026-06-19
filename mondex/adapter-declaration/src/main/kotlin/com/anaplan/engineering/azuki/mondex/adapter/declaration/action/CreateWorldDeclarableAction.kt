package com.anaplan.engineering.azuki.mondex.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.mondex.adapter.api.CreateWorldBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState

open class CreateWorldDeclarableAction(
    protected val authPurses: Map<String, Pair<ULong, ULong>>,
    protected val worldName: String = MondexDeclarationState.DEFAULT_WORLD,
) : CreateWorldBehaviour(), DeclarableAction<MondexDeclarationState> {

    override fun declare(state: MondexDeclarationState) = state.declareWorld(worldName, authPurses)
}
