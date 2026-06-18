package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

class RightOfWayPairs : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun safeAirspaceIsValid() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            everythingIsOkay()
        }
    }

    @AnalysisScenario
    fun rightOfWayFromJsonAirspace() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasAircraft(airspaceUK, a0)
            hasAircraft(airspaceUK, a1)
            hasRightOfWay(airspaceUK, a1, a0)
        }
    }
}
