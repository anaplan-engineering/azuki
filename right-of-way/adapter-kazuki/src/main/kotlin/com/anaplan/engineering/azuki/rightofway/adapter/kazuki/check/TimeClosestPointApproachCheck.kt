package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class TimeClosestPointApproachCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val t: Double)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.TCPA)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = functions.tCPA(aircraft0, aircraft1)
        if (r != t) {Log.error("TCPA check failed: expected `$t` found `$r`")}
        return r == t
    }
}
