package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace_Module.mk_Airspace
import com.anaplan.engineering.kazuki.core.mk_Set

class StartAirspaceAction(airspaceName: String, delta_o: Double, delta_c: Double, theta_h: Double, opened: Boolean) :
    StartAirspaceDeclarableAction(airspaceName, delta_o, delta_c, theta_h, opened), KazukiAction {

    // start an empty airspace with default constants
    override fun act(env: ExecutionEnvironment) {
        env.set(airspaceName, mk_Airspace(mk_Set(), delta_o, delta_c, theta_h, opened))
    }
}
