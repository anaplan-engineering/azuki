package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import com.anaplan.engineering.azuki.rightofway.implementation.position

// TODO this looks ugly. Either change name, or how best to map these "api-level" x "impl-level" enums?
fun Quadrant.toQuadrant() = com.anaplan.engineering.azuki.rightofway.implementation.QuadrantImpl.entries[ordinal]

class QuadrantCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val q: Quadrant)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.OnQuadrant)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = quadrant(aircraft0, aircraft1.position())
        if (r != q.toQuadrant()) {Log.error("QuadrantImpl check failed: expected `$q` found `$r`")}
        return r == q.toQuadrant()
    }
}
