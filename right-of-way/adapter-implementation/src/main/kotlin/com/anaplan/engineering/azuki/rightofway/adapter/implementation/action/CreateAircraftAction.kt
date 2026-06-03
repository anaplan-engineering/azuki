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
            //TODO when is this@qualifier needed? get there are two "this" in context but not needed here, added for clarity
//            addAircraft(aircraftName, this@CreateAircraftAction.position.toPair(),
//                this@CreateAircraftAction.velocity.toPair())

            // adds an aircraft in fresh position and velocity
            addAircraft(aircraftName)
        }
    }
}
