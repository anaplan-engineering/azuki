package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace

class QuadrantConvergenceCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val c: Convergence)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.QuadrantConvergence)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft) =
        when (c) {
            Convergence.Convergence -> functions.QC(aircraft0, aircraft1)
            Convergence.Overtake -> functions.QO(aircraft0, aircraft1)
            Convergence.Divergence -> functions.QD(aircraft0, aircraft1)
        }
}

