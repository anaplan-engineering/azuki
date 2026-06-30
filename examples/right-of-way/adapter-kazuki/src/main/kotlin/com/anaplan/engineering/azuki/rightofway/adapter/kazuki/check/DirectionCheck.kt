package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class DirectionCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val d: Boolean)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.Direction)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        if (d) functions.same_orientation(aircraft0, aircraft1) else functions.opposite_orientation(aircraft0, aircraft1)
}

