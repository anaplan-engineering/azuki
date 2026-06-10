package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment

class StartAirspaceAction(airspaceName: String) :
    StartAirspaceDeclarableAction(airspaceName), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        TODO("Not yet implemented")
    }

}
