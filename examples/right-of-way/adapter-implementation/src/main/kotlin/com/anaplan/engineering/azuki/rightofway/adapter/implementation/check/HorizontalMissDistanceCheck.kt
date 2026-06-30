package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.api.approxEqual
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class HorizontalMissDistanceCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val d: Double)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.HMD)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = horizontalMissDistance(aircraft0, aircraft1)
        val result = approxEqual(r, d)
        if (!result) {Log.error("Horizontal miss distance check failed: expected `$d` found `$r`")}
        return result
    }
}
