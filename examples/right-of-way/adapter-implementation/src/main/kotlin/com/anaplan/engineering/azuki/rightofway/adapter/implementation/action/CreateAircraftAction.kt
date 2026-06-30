package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class CreateAircraftAction(
    airspaceName: String,
    aircraftName: String,
    aircraft: Aircraft,
) : CreateAircraftDeclarableAction(airspaceName, aircraftName, aircraft), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withAirspace(airspaceName) {
            // Transform the adapter-api type to the implementation type
            addAircraft(aircraftName, aircraft.position.toPair(), aircraft.velocity.toPair())
        }
    }
}
