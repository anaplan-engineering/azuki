package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class TrackCheck(private val airspaceName: String, private val aircraft: String, private val t: Double)
    : AbstractCheck(airspaceName, aircraft, aircraft, RightOfWayBehaviours.OnTrack)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean {
        if (t !in 0.0..360.0) {Log.error("Track check requires a valid angle between 0..360 degrees, found $t")}
        val r = track(aircraft1)
        if (r != t) {Log.error("Track check failed: expected `$t` found `$r`")}
        return r == t
    }
}
