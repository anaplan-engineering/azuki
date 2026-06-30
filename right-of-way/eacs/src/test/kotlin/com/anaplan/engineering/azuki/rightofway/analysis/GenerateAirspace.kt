package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayOracleScenarioImpl

/**
 * Exercises the `generate { }` DSL block. Requires an action-generator-capable oracle runner
 * (SampleImpl), not the standard [com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario] EACS runner.
 */
class GenerateAirspace : RightOfWayOracleScenarioImpl() {

    fun generateEmptyAirspace() {
        generate {
            generateAirspace(airspaceUK)
        }
    }

    fun generateAirspaceWithAircraft() {
        generate {
            generateAirspaceWithAircraft(airspaceUK, numberOfAirCraft = 2U)
        }
    }

    fun generateThenVerify() {
        generate {
            generateAirspace(airspaceUK)
        }
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        verify {
            airspaceHasAircraft(airspaceUK, a0)
            airspaceHasAircraft(airspaceUK, a1)
        }
    }
}
