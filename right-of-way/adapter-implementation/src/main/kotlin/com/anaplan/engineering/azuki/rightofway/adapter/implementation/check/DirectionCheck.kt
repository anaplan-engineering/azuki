package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Direction
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

// TODO this looks ugly. Either change name, or how best to map these "api-level" x "impl-level" enums?
fun Direction.toDirection() = com.anaplan.engineering.azuki.rightofway.implementation.DirectionImpl.entries[ordinal]

class DirectionCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val d: Direction)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.Direction)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = direction(aircraft0, aircraft1)
        if (r != d.toDirection()) { Log.error("DirectionImpl check failed: expected `$d` found `$r`")}
        return r == d.toDirection()
    }
}

