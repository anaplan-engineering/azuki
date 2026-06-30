
package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import org.slf4j.LoggerFactory

class HasRightOfWayCheck(
    private val airspaceName: String,
    private val withRightOfWay: String,
    private val givingWay: String,
) : ReifiedBehavior, SampleCheck {

    override val behavior = RightOfWayBehaviours.RightOfWay

    override fun check(env: ExecutionEnvironment): Boolean {
        if (airspaceName !in env.airspaceManager.activeAirspaces) {
            Log.error("Airspace $airspaceName not found")
            return false
        }
        return env.withAirspace(airspaceName) {
            if (!hasAircraft(withRightOfWay) || !hasAircraft(givingWay)) {
                Log.error("Aircraft $withRightOfWay or $givingWay not found")
                false
            } else {
                Log.info("Checking airspace $airspaceName for $withRightOfWay and $givingWay ${this@HasRightOfWayCheck::class.simpleName}")
                hasRightOfWay(getAircraft(withRightOfWay), getAircraft(givingWay))
            }
        }
    }

    private val Log = LoggerFactory.getLogger(this::class.java)
}
