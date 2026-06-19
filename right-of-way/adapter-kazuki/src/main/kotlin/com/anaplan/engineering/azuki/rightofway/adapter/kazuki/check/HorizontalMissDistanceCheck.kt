package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.api.approxEqual
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class HorizontalMissDistanceCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val d: Double)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.HMD)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = functions.horizontalMissDistance(aircraft0, aircraft1)
        if (!approxEqual(r, d)) {Log.error("Horizontal miss distance check failed: expected `$d` found `$r`")}
        return approxEqual(r, d)
    }
}
