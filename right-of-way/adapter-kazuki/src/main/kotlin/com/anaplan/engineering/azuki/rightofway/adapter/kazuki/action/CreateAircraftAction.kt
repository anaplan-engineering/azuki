package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.toKazuki

class CreateAircraftAction(airspaceName: String, aircraftName: String, aircraft: Aircraft) :
    CreateAircraftDeclarableAction(airspaceName, aircraftName, aircraft), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        val airspace = env.airspace(airspaceName)
        env.setAircraft(airspaceName, aircraftName, aircraft)
        env.set(airspaceName, airspace.functions.addAircraft(aircraft.toKazuki()))
    }
}
