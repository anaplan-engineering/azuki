package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.ReifiedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import org.slf4j.LoggerFactory

abstract class AbstractCheck(
    private val airspaceName: String,
    private val aircraft0: String,
    private val aircraft1: String,
    override val behavior: Behavior,
) : ReifiedBehavior, SampleCheck {

    final override fun check(env: ExecutionEnvironment): Boolean {
        if (airspaceName in env.airspaceManager.activeAirspaces) {
            return env.withAirspace(airspaceName) {
                if (hasAircraft(aircraft0) && hasAircraft(aircraft1)) {
                    Log.info("Checking airspace $airspaceName for $aircraft0 and $aircraft1 ${this@AbstractCheck::class.simpleName}")
                    booleanCheck(getAircraft(aircraft0), getAircraft(aircraft1))
                }
                else {
                    Log.error("Aircraft $aircraft0 or $aircraft1 not found")
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
    protected abstract fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean

    protected val Log = LoggerFactory.getLogger(this::class.java)

}
