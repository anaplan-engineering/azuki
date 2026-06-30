package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Crossing
import com.anaplan.engineering.azuki.rightofway.adapter.api.toRightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft

class CrossingCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val crossing: Crossing)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, crossing.toRightOfWayBehaviours())
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        when(crossing) {
            Crossing.Crossing -> functions.going_to_cross(aircraft0, aircraft1)
            Crossing.Crossed -> functions.crossed(aircraft0, aircraft1)
            Crossing.ZeroCrossed -> functions.zero_crossed(aircraft0, aircraft1)
            Crossing.OneCrossed -> functions.one_crossed(aircraft0, aircraft1)
            Crossing.BothCrossed -> functions.both_crossed(aircraft0, aircraft1)
        }
}
