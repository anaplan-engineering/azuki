package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class StartAirspaceAction(airspaceName: String, delta_o: Double, delta_c: Double, theta_h: Double, opened: Boolean) :
    StartAirspaceDeclarableAction(airspaceName, delta_o, delta_c, theta_h, opened), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.airspaceManager.add(airspaceName,
            env.airspaceManager.airspaceCreator.create(delta_o, delta_c, theta_h, opened))
    }
}
