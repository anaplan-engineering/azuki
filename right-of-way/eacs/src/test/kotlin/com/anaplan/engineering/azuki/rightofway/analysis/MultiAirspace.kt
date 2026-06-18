package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.adapter.api.aircraft
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.airspaceUS
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import com.anaplan.engineering.azuki.rightofway.position0
import com.anaplan.engineering.azuki.rightofway.velocity0

class MultiAirspace : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun twoIndependentAirspaces() {
        given {
            thereIsAnAirspace(airspaceUK) {
                thereIsAnAircraft(a0, aircraft(position0 to velocity0))
            }
            thereIsANewAirspace(airspaceUS)
        }
        then {
            aircraftCount(airspaceUK, 1U, true)
            aircraftCount(airspaceUS, 0U, true)
            hasAircraft(airspaceUK, a0)
        }
    }
}
