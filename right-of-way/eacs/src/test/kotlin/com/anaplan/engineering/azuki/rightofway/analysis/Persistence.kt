package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.aircraft0
import com.anaplan.engineering.azuki.rightofway.aircraft1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

class Persistence : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun savingAirspace() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasRightOfWay(airspaceUK, aircraft0, aircraft1)
        }
        regardlessOf {
            saveAirspace(airspaceUK)
        }
    }

    @AnalysisScenario
    fun reopenAirspace() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasRightOfWay(airspaceUK, aircraft0, aircraft1)
        }
        regardlessOf {
            saveAirspace(airspaceUK)
            closeAirspace(airspaceUK)
            loadAirspace(airspaceUK)
        }
    }
}


