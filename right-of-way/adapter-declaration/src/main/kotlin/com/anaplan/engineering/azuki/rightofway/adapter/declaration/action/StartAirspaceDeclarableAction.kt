package com.anaplan.engineering.azuki.rightofway.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.StartAirspaceBehaviour
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState

open class StartAirspaceDeclarableAction(protected val airspaceName: String, protected val opened: Boolean) :
        StartAirspaceBehaviour(), DeclarableAction<RightOfWayDeclarationState> {
    override fun declare(state: RightOfWayDeclarationState) = state.declareAirspace(airspaceName, opened = opened)
}

