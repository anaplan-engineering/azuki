package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class QuadrantCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val q: Quadrant)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.OnQuadrant)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        when (q) {
            Quadrant.FRONT_RIGHT -> functions.Q1(aircraft0, aircraft1.position)
            Quadrant.FRONT_LEFT  -> functions.Q2(aircraft0, aircraft1.position)
            Quadrant.BACK_LEFT   -> functions.Q3(aircraft0, aircraft1.position)
            Quadrant.BACK_RIGHT  -> functions.Q4(aircraft0, aircraft1.position)
        }
}
