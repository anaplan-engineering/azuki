package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import com.anaplan.engineering.azuki.rightofway.implementation.Quadrant
import com.anaplan.engineering.azuki.rightofway.implementation.position

class QuadrantCheck(private val airspaceName: String, private val aircraft1: String, private val aircraft2: String, private val q: Quadrant)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.OnQuadrant)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean {
        val r = quadrant(aircraft1, aircraft2.position())
        if (r != q) {Log.error("Quadrant check failed: expected `$q` found `$r`")}
        return r == q
    }
}
