package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

class AirspaceConstants : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun customDeltaConstants() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace, delta_o = 50.0, delta_c = 500.0, theta_h = 45.0)
        }
        then {
            aircraftCount(airspaceUK, 2U, true)
        }
    }

    @AnalysisScenario
    fun startsClosedThenOpens() {
        given {
            thereIsANewAirspace(airspaceUK, opened = false)
        }
        then {
            aircraftCount(airspaceUK, 0U, false)
        }
        whenever {
            setAirspace(airspaceUK, true)
        }
        then {
            aircraftCount(airspaceUK, 0U, true)
        }
    }
}
