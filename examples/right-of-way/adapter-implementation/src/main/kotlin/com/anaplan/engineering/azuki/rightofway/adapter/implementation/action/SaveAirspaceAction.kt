package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class SaveAirspaceAction (
    private val airspaceName: String,
) : SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.airspaceManager.save(airspaceName)
    }

    override val behavior = RightOfWayBehaviours.SaveAirspace
}
