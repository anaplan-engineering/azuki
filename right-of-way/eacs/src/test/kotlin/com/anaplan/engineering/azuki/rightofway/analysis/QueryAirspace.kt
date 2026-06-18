package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayQueryScenarioImpl

/**
 * Exercises the `query { }` DSL block. Requires a query-capable runner (e.g. oracle / script-runner),
 * not the standard [com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario] EACS runner.
 */
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
