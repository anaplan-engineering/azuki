package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayOracleScenarioImpl

// TODO LF: instrumenting a oracle scenario
class VerifyAirspace : RightOfWayOracleScenarioImpl() {

    fun verifyAircraftAndRightOfWay() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        verify {
            airspaceHasAircraft(airspaceUK, a0)
            airspaceHasAircraft(airspaceUK, a1)
            hasRightOfWay(airspaceUK, a1, a0)
        }
    }

    fun forAllAircraftNames() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        verify {
            forAll({ aircraftNamesIn(airspaceUK) }) { name ->
                airspaceHasAircraft(airspaceUK, name)
            }
        }
    }

    fun forAllRightOfWayPairs() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        verify {
            forAll({ hasRightOfWay(airspaceUK) }) { pair ->
                hasRightOfWay(airspaceUK, pair.first, pair.second)
            }
        }
    }
}
