package com.anaplan.engineering.azuki.rightofway.adapter.declaration.action

import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.CreateAircraftBehaviour
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState

open class CreateAircraftDeclarableAction(
    protected val airspaceName: String,
    protected val aircraftName: String,
    protected val position: Position,
    protected val velocity: Velocity
) :
    CreateAircraftBehaviour(), DeclarableAction<RightOfWayDeclarationState> {
    override fun declare(state: RightOfWayDeclarationState) =
        state.declareAircraft(airspaceName, aircraftName, position, velocity)
}
