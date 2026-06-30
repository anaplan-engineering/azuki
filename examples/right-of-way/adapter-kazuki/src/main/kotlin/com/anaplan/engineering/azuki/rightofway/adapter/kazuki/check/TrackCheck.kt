package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.api.approxEqual
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class TrackCheck(airspaceName: String, aircraft: String, private val t: Double)
    : AbstractCheck(airspaceName, aircraft, aircraft, RightOfWayBehaviours.OnTrack)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = functions.track(aircraft0)
        if (!approxEqual(r, t)) {Log.error("Track check failed: expected `$t` found `$r`")}
        return approxEqual(r, t)
    }
}
