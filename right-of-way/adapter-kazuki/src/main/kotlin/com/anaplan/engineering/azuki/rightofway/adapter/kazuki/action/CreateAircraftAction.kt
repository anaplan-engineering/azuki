package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.toKazuki
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft_Module.mk_Aircraft

class CreateAircraftAction(airspaceName: String, aircraftName: String, position: Position, velocity: Velocity) :
    CreateAircraftDeclarableAction(airspaceName, aircraftName, position, velocity), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        val airspace = env.airspace(airspaceName)
        val aircraft = mk_Aircraft(position.toKazuki(), velocity.toKazuki())
        // Kazuki spec doesn't need aircraft names, but save it anyhow
        env.set("${airspaceName}.${airspaceName}", aircraft)
        env.set(airspaceName, airspace.functions.addAircraft(aircraft))
    }
}
