package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import com.anaplan.engineering.azuki.rightofway.implementation.Convergence

class QuadrantConvergenceCheck(private val airspaceName: String, private val aircraft1: String, private val aircraft2: String, private val c: Convergence)
    : AbstractCheck(airspaceName, aircraft1, aircraft2, RightOfWayBehaviours.QuadrantConvergence)
{
    override fun Airspace.booleanCheck(aircraft1: Aircraft, aircraft2: Aircraft): Boolean {
        val r = quadrantConvergence(aircraft1, aircraft2)
        if (r != c) {Log.error("Quadrant convergence check failed: expected `$c` found `$r`")}
        return r == c
    }
}

