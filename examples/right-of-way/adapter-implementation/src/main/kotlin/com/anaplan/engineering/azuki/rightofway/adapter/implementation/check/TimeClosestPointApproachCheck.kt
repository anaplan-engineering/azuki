package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class TimeClosestPointApproachCheck(airspaceName: String, aircraft0: String,
                                    aircraft1: String, private val t: Double)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.TCPA)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        if (t >= 0.0) {Log.error("TCPA check requires a negative input, found $t")}
        val r = timeToClosestPointApproach(aircraft0, aircraft1)
        if (r != t) {Log.error("TCPA check failed: expected `$t` found `$r`")}
        return r == t
    }
}
