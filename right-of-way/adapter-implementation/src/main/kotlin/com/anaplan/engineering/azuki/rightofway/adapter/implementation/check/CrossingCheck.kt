package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

enum class Crossing { Crossing, Crossed, ZeroCrossed, OneCrossed, BothCrossed }

class CrossingCheck(private val airspaceName: String, private val aircraft1: String, private val aircraft2: String, private val crossing: Crossing)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.ConvergeHeadon)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft) =
        when(crossing) {
            Crossing.Crossing -> crossing(aircraft1, aircraft2)
            Crossing.Crossed -> crossed(aircraft1, aircraft2)
            Crossing.ZeroCrossed -> zeroCrossed(aircraft1, aircraft2)
            Crossing.OneCrossed -> oneCrossed(aircraft1, aircraft2)
            Crossing.BothCrossed -> bothCrossed(aircraft1, aircraft2)
        }
}
