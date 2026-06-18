package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.adapter.api.aircraft
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
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

    @AnalysisScenario
    fun airspaceDataClosedThenOpen() {
        given {
            // creates a new closed airspace with 5 aircraft. This will not be safe
            thereIsAnAirspace(airspaceUK, a0a1Airspace, opened = false)
        }
        whenever {
            setAirspace(airspaceUK, true)
        }
        then {
            aircraftCount(airspaceUK,2U, true)
        }
    }

    @AnalysisScenario
    fun airspaceDataOpen() {
        given {
            // creates a new closed airspace with 5 aircraft. This will not be safe
            thereIsAnAirspace(airspaceUK, a0a1Airspace, opened = true)
        }
        then {
            aircraftCount(airspaceUK,2U, true)
        }
    }

//    @AnalysisScenario
//    fun airspaceSpiralPositionedAircraft() {
//        given {
//            // creates a new closed airspace with 5 aircraft. This will not be safe
//            thereIsANewAirspaceWithAircraft(airspaceUK, numberOfAircraft = 5U)
//        }
//        then {
//            aircraftCount(airspaceUK,5U, true)
//        }
//    }
}
