package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class StartAirspaceAction(airspaceName: String) : StartAirspaceDeclarableAction(airspaceName), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withAirspace(airspaceName) {
            println(aircraftIds)
            println(positions)
        }
        env.airspaceManager.add(airspaceName, env.airspaceManager.airspaceCreator.create())
    }
}
