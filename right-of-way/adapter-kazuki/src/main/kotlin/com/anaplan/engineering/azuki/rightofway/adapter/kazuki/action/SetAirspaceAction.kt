package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.SetAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace_Module.transform

class SetAirspaceAction(airspaceName: String, opened: Boolean) :
    SetAirspaceDeclarableAction(airspaceName, opened), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        val airspace = env.airspace(airspaceName)
        env.set(airspaceName, airspace.transform(opened = opened))
    }
}
