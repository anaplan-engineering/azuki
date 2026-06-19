package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayQueryScenarioImpl

// TODO LF: instrumenting a query scenario
class QueryAirspace : RightOfWayQueryScenarioImpl() {

    fun queryAllAircraftAndMembership() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        query {
            allAircraftsIn(airspaceUK)
            aircraftNamesIn(airspaceUK)
            airspaceHasAircraft(airspaceUK, a0)
            airspaceHasAircraft(airspaceUK, a1)
            hasRightOfWay(airspaceUK)
            hasRightOfWay(airspaceUK, a1, a0)
        }
    }
}
