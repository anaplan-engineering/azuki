package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class OvertakingCheck(
    airspaceName: String, aircraft0: String, aircraft1: String,
) : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.Overtaking)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        overtaking(aircraft0, aircraft1)
}

