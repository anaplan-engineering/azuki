package com.anaplan.engineering.azuki.rightofway.adapter.implementation.check

import com.anaplan.engineering.azuki.rightofway.adapter.api.Convergence
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.implementation.Aircraft
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

// TODO this looks ugly. Either change name, or how best to map these "api-level" x "impl-level" enums?
fun Convergence.toConvergence() = com.anaplan.engineering.azuki.rightofway.implementation.ConvergenceImpl.entries[ordinal]

class QuadrantConvergenceCheck(airspaceName: String, aircraft0: String, aircraft1: String, private val c: Convergence)
    : AbstractCheck(airspaceName, aircraft0, aircraft1, RightOfWayBehaviours.QuadrantConvergence)
{
    override fun Airspace.booleanCheck(aircraft0: Aircraft, aircraft1: Aircraft): Boolean {
        val r = quadrantConvergence(aircraft0, aircraft1)
        if (r != c.toConvergence()) {Log.error("QuadrantImpl convergence check failed: expected `$c` found `$r`")}
        return r == c.toConvergence()
    }
}

