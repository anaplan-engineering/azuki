
package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import org.slf4j.LoggerFactory

class HasAircraftCheck(private val airspaceName: String, private val aircraftName: String,
) : ReifiedBehavior, SampleCheck {
    override val behavior = RightOfWayBehaviours.HasAircraft
    override fun check(env: ExecutionEnvironment): Boolean {
        if (airspaceName in env.airspaceManager.activeAirspaces) {
            return env.withAirspace(airspaceName) {
                hasAircraft(aircraftName)
            }
        }
        else {
            Log.error("Airspace $airspaceName not found")
            return false
        }
    }
    private val Log = LoggerFactory.getLogger(this::class.java)
}
