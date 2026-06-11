package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class CreateAircraftAction(
    airspaceName: String,
    aircraftName: String,
    position: Position,
    velocity: Velocity,
) : CreateAircraftDeclarableAction(airspaceName, aircraftName, position, velocity), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withAirspace(airspaceName) {
            // Transform the adapter-api type to the implementation type
            addAircraft(aircraftName, position.toPair(), velocity.toPair())
        }
    }
}
