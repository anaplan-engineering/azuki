package com.anaplan.engineering.azuki.rightofway.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.SetAirspaceBehaviour
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState

open class SetAirspaceDeclarableAction(
    protected val airspaceName: String,
    protected val opened: Boolean,
) : SetAirspaceBehaviour(), DeclarableAction<RightOfWayDeclarationState> {
    override fun declare(state: RightOfWayDeclarationState) =
        state.setAirspace(airspaceName, opened)
}
