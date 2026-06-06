package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class TimeClosestPointApproachCheck(private val airspaceName: String, private val aircraft1: String,
                                    private val aircraft2: String, private val t: Double)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.TCPA)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean {
        if (t >= 0.0) {Log.error("TCPA check requires a negative input, found $t")}
        val r = timeToClosestPointApproach(aircraft1, aircraft2)
        if (r != t) {Log.error("TCPA check failed: expected `$t` found `$r`")}
        return r == t
    }
}
