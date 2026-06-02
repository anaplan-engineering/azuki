package com.anaplan.engineering.azuki.rightofway.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.NewAirspaceBehaviour
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState

open class NewAirspaceDeclarableAction(protected val airSpaceName: String) :
        NewAirspaceBehaviour(), DeclarableAction<RightOfWayDeclarationState> {
    override fun declare(state: RightOfWayDeclarationState) = state.declareAirspace(airSpaceName)
}

