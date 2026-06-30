package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.SetAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class SetAirspaceAction(airspaceName: String, opened: Boolean) :
    SetAirspaceDeclarableAction(airspaceName, opened), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.withAirspace(airspaceName) { setAirspace(opened) }
    }
}
