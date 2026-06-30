package com.anaplan.engineering.azuki.rightofway.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.StartAirspaceBehaviour
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState

open class StartAirspaceDeclarableAction(protected val airspaceName: String,
                                         protected val delta_o: Double,
                                         protected val delta_c: Double,
                                         protected val theta_h: Double,
                                         protected val opened: Boolean) :
        StartAirspaceBehaviour(), DeclarableAction<RightOfWayDeclarationState> {
    override fun declare(state: RightOfWayDeclarationState) =
        state.declareAirspace(airspaceName, delta_o, delta_c, theta_h, opened)
}

