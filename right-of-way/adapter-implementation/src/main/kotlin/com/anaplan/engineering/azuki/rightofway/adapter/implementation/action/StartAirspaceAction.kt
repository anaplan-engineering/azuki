package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class StartAirspaceAction(airspaceName: String, opened: Boolean) : StartAirspaceDeclarableAction(airspaceName, opened), SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.airspaceManager.add(airspaceName,
            env.airspaceManager.airspaceCreator.create(
                deltaO = DELTA_O, deltaC = DELTA_C, thetaH = THETA_H, opened = opened))
    }
}
