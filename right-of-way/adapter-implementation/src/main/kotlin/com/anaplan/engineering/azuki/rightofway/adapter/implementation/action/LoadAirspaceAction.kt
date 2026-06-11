package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class LoadAirspaceAction(
    private val airspaceFileName: String,
) : SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.airspaceManager.load(airspaceFileName)
    }

    override val behavior = RightOfWayBehaviours.LoadAirspace
}
