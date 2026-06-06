package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class HeadOnCheck(private val airspaceName: String, private val aircraft1: String, private val aircraft2: String)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.ConvergeHeadon)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft) = headon(aircraft1, aircraft2)
}
