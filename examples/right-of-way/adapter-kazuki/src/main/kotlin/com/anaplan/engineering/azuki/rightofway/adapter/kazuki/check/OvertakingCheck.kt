package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class OvertakingCheck(airspaceName: String, aircraft0: String, aircraft1: String,
) : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.Overtaking)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        functions.overtaking(aircraft0, aircraft1)(delta_o)
}

