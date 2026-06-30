package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Crossing
import com.anaplan.engineering.azuki.rightofway.adapter.api.toRightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class CrossingCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val crossing: Crossing)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, crossing.toRightOfWayBehaviours())
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        when(crossing) {
            Crossing.Crossing -> crossing(aircraft0, aircraft1)
            Crossing.Crossed -> crossed(aircraft0, aircraft1)
            Crossing.ZeroCrossed -> zeroCrossed(aircraft0, aircraft1)
            Crossing.OneCrossed -> oneCrossed(aircraft0, aircraft1)
            Crossing.BothCrossed -> bothCrossed(aircraft0, aircraft1)
        }
}
