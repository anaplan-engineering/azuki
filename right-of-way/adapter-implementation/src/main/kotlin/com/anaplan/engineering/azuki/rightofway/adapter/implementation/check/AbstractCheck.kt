package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import org.slf4j.LoggerFactory

abstract class AbstractCheck(
    private val airspaceName: String,
    private val aircraft1: String,
    private val aircraft2: String,
    override val behavior: Behavior,
) : ReifiedBehavior, SampleCheck {

    final override fun check(env: ExecutionEnvironment): Boolean {
        if (airspaceName in env.airspaceManager.activeAirspaces) {
            return env.withAirspace(airspaceName) {
                if (hasAircraft(aircraft1) && hasAircraft(aircraft2)) {
                    Log.info("Checking airspace $airspaceName for $aircraft1 and $aircraft2 ${this@AbstractCheck::class.simpleName}")
                    booleanCheck(getAircraft(aircraft1), getAircraft(aircraft2))
                }
                else {
                    Log.error("Aircraft $aircraft1 or $aircraft2 not found")
                    false
                }
            }
        }
        else {
            Log.error("Airspace $airspaceName not found")
            return false
        }
    }

    //TODO best way to allow for easy extension?
    protected abstract fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean

    protected val Log = LoggerFactory.getLogger(this::class.java)

}
