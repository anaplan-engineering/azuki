package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.adapter.api.aircraft
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import com.anaplan.engineering.azuki.rightofway.position0
import com.anaplan.engineering.azuki.rightofway.velocity0

class RegardlessOfActions : RightOfWayRunnableScenario() {

    //TODO
    //@AnalysisScenario
    fun addAircraftRegardlessOfChecks() {
        given {
            thereIsANewAirspace(airspaceUK)
        }
        then {
            aircraftCount(airspaceUK, 0U, true)
        }
        regardlessOf {
            addAircraft(airspaceUK, a0, aircraft(position0 to velocity0))
        }
    }
}
