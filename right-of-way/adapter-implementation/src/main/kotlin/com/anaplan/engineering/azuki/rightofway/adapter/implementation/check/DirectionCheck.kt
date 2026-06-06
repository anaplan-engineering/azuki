package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import com.anaplan.engineering.azuki.rightofway.implementation.Direction

class DirectionCheck(private val airspaceName: String, private val aircraft1: String, private val aircraft2: String, private val d: Direction)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.Direction)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean {
        val r = direction(aircraft1, aircraft2)
        if (r != d) { Log.error("Direction check failed: expected `$d` found `$r`")}
        return r == d
    }
}

