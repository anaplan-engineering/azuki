package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment

class CreateAircraftAction(airspaceName: String, aircraftName: String, position: Position, velocity: Velocity) :
    CreateAircraftDeclarableAction(airspaceName, aircraftName, position, velocity), KazukiAction {
    override fun act(env: ExecutionEnvironment) {
        TODO("Not yet implemented")
    }
}
