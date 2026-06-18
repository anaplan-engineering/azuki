package com.anaplan.engineering.azuki.rightofway.eacs

import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayBehaviours
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayFunctionalElements
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

@BEH(RightOfWayBehaviours.NewAircraft, RightOfWayFunctionalElements.Airspace, """
    Start a new airspace
""")
class BEH1 : RightOfWayRunnableScenario() {

    //@Eac("When an airspace is started with some aircraft, it is populated properly and has right of way")
    fun noNewAircrafts() {
        given {
            thereIsANewAirspace(airspaceUK)
        }
        then {
            hasAircraft(airspaceUK, a0)
            hasAircraft(airspaceUK, a1)
        }
    }

}
