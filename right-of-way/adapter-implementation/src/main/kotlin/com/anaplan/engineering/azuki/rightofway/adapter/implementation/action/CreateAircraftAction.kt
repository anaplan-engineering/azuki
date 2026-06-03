package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class CreateAircraftAction(
    airspaceName: String,
    aircraftName: String,
    //val position: Position,
    //val velocity: Velocity,
) : CreateAircraftDeclarableAction(airspaceName, aircraftName), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withAirspace(airspaceName) {

            // adds an aircraft in fresh position and velocity
            addAircraft(aircraftName)
        }
    }
}
