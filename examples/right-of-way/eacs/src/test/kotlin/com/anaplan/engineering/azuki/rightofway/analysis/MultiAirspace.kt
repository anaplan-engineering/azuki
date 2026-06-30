package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.adapter.api.aircraft
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.airspaceUS
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import com.anaplan.engineering.azuki.rightofway.position0
import com.anaplan.engineering.azuki.rightofway.position1
import com.anaplan.engineering.azuki.rightofway.velocity0
import com.anaplan.engineering.azuki.rightofway.velocity1

class MultiAirspace : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun twoIndependentAirspaces() {
        given {
            thereIsAnAirspace(airspaceUK) {
                thereIsAnAircraft(a0, aircraft(position0 to velocity0))
            }
            // repeated aircraft in multiple airspaces is not considered here
            thereIsAnAirspace(airspaceUS) {
                thereIsAnAircraft(a0, aircraft(position0 to velocity0))
                thereIsAnAircraft(a1, aircraft(position1 to velocity1))
            }
        }
        then {
            aircraftCount(airspaceUK, 1U, true)
            aircraftCount(airspaceUS, 2U, true)
            hasAircraft(airspaceUK, a0)
            hasAircraft(airspaceUS, a1)
        }
    }
}
