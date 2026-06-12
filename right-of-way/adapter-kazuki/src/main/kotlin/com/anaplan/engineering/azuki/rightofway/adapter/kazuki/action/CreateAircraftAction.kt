package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft_Module.mk_Aircraft

class CreateAircraftAction(airspaceName: String, aircraftName: String, aircraft: Aircraft) :
    CreateAircraftDeclarableAction(airspaceName, aircraftName, aircraft), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        val airspace = env.airspace(airspaceName)
        // Transform the adapter-api type to the kazuki type
        val aircraft = mk_Aircraft(aircraft.position.toKazuki(), aircraft.velocity.toKazuki())
        // Kazuki spec doesn't need aircraft names, but save it anyhow
        env.set("${airspaceName}.${airspaceName}", aircraft)
        env.set(airspaceName, airspace.functions.addAircraft(aircraft))
    }
}
