package com.anaplan.engineering.azuki.rightofway.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayFunctionalElements
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

//TODO LF need further understanding of the relationship beteen behaviours / functionaal elements / EACS and other type of scenarios
@BEH(RightOfWayBehaviours.NewAircraft, RightOfWayFunctionalElements.Airspace, """
    Start a new airspace
""")
class BEH1 : RightOfWayRunnableScenario() {

    @Eac("When an airspace is started with some aircraft, it is populated properly and has right of way")
    fun noNewAircrafts() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasAircraft(airspaceUK, a0)
            hasAircraft(airspaceUK, a1)
            aircraftCount(airspaceUK, 2U, true)
        }
    }

}
