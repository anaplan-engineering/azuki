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
        // TODO LF: Kazuki spec doesn't need aircraft names, but save it anyhow? If so, build has to do the same
        //env.set("${airspaceName}_${aircraftName}", aircraft)
        // Transform the adapter-api type to the kazuki type
        env.set(airspaceName, airspace.functions.addAircraft(aircraft.toKazuki()))
    }
}
