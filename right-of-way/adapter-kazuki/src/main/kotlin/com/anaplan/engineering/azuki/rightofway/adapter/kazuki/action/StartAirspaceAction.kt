package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace_Module.mk_Airspace
import com.anaplan.engineering.kazuki.core.mk_Set

class StartAirspaceAction(airspaceName: String) :
    StartAirspaceDeclarableAction(airspaceName), KazukiAction {

    // start an empty airspace with default constants
    override fun act(env: ExecutionEnvironment) {
        mk_Airspace(mk_Set()).let { env.set(airspaceName, it) }
    }
}
