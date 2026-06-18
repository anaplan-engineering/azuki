package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

class Persistence : RightOfWayRunnableScenario() {

//    @AnalysisScenario
    fun savingAirspace() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasRightOfWay(airspaceUK, a1, a0)
        }
        regardlessOf {
            saveAirspace(airspaceUK)
        }
    }

//    @AnalysisScenario
    fun reopenAirspace() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasRightOfWay(airspaceUK, a1, a0)
        }
        regardlessOf {
            saveAirspace(airspaceUK)
            unloadAirspace(airspaceUK)
            loadAirspace(airspaceUK)
        }
    }
}


