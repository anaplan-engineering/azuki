package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class CloseAirspaceAction(
    private val airspaceName: String,
) : SampleAction {

    override fun act(env: ExecutionEnvironment) {
        env.airspaceManager.close(airspaceName)
    }

    override val behavior = unsupportedBehavior
}
