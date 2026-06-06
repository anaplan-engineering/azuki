package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class HorizontalMissDistanceCheck(private val airspaceName: String, private val aircraft1: String,
                                  private val aircraft2: String, private val d: Double)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.HMD)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean {
        val r = horizontalMissDistance(aircraft1, aircraft2)
        if (r != d) {Log.error("Horizontal miss distance check failed: expected `$d` found `$r`")}
        return r == d
    }
}
