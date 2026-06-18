package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.adapter.api.aircraft
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import com.anaplan.engineering.azuki.rightofway.position0
import com.anaplan.engineering.azuki.rightofway.position1
import com.anaplan.engineering.azuki.rightofway.velocity0
import com.anaplan.engineering.azuki.rightofway.velocity1

class NewAirspace : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun emptyAirspace() {
        given {
            thereIsANewAirspace(airspaceUK)
        }
        then {
            aircraftCount(airspaceUK,0U, true)
        }
    }

    @AnalysisScenario
    fun airspaceWithAircraft() {
        given {
            thereIsAnAirspace(airspaceUK) {
                thereIsAnAircraft(a0, aircraft(position0 to velocity0))
                thereIsAnAircraft(a1, aircraft(position1 to velocity1))
            }
        }
        then {
            aircraftCount(airspaceUK,2U, true)
        }
    }

    @AnalysisScenario
    fun airspaceAddAircraft() {
        given {
            thereIsANewAirspace(airspaceUK)
        }
        whenever {
            placeAircraft(airspaceUK, a0, aircraft(position0 to velocity0))
            placeAircraft(airspaceUK, a1, aircraft(position1 to velocity1))
        }
        then {
            aircraftCount(airspaceUK,2U, true)
        }
    }

}
