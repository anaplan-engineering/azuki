package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.api.approxEqual
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class TrackCheck(airspaceName: String, aircraft: String, private val t: Double)
    : AbstractCheck(airspaceName, aircraft, aircraft, RightOfWayBehaviours.OnTrack)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        if (t !in 0.0..360.0) {Log.error("Track check requires a valid angle between 0..360 degrees, found $t")}
        val r = track(aircraft0)
        val result = approxEqual(r, t)
        if (!result) {Log.error("Track check failed: expected `$t` found `$r`")}
        return result
    }
}
