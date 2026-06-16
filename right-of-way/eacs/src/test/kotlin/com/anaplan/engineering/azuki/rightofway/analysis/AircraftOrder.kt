package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.runner.ToBeDone
import com.anaplan.engineering.azuki.rightofway.Aircraft0
import com.anaplan.engineering.azuki.rightofway.Aircraft1
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayWhen
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.aircraft0
import com.anaplan.engineering.azuki.rightofway.aircraft1
import com.sun.tools.doclint.Entity
import org.junit.runners.Parameterized

class AircraftOrder(private val testCase: TestCase) : RightOfWayRunnableScenario() {

    companion object {

        data class TestCase(val description: String, val moves: RightOfWayWhen.() -> Unit) {
            override fun toString() = description
        }

        private fun testCase(
            description: String,
            knownBug: KnownBug? = null,
            toBeDone: ToBeDone? = null,
            moves: RightOfWayWhen.() -> Unit,
        ): Array<Any> = arrayOf(TestCase(description, moves), *listOfNotNull(knownBug, toBeDone).toTypedArray())

        @Parameterized.Parameters(name = "{0}")
        fun actions() = listOf(
            testCase("a0") {
                placeAircraft(airspaceUK, aircraft0, Aircraft0)
            },
            testCase("a0 + a1") {
                placeAircraft(airspaceUK, aircraft0, Aircraft0)
                placeAircraft(airspaceUK, aircraft1, Aircraft1)
            },
            testCase("a0 + a0", KnownBug(Issue("SampleImpl", "FOO-123"), Issue("VDM", "FOO-234"))) {
                placeAircraft(airspaceUK, aircraft0, Aircraft0)
                placeAircraft(airspaceUK, aircraft0, Aircraft0)
            },
            testCase("a1 + a0", toBeDone = ToBeDone(Issue("SampleImpl", "BAR-567"))) {
                placeAircraft(airspaceUK, aircraft1, Aircraft1)
                placeAircraft(airspaceUK, aircraft0, Aircraft0)
            }
        )
    }

    @AnalysisScenario
    fun equivalentAircraftOrder() {
        val testCase = this.testCase
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        whenever {
            testCase.moves(this)
        }
        then {
            hasAircraft(airspaceUK, aircraft0)
            hasAircraft(airspaceUK, aircraft1)
        }
    }

    fun foo() {
        given {
            thereIsAnAirspace(airspaceUK) {
                // add to the DSL to build the aircraft
                thereIsAnAircraft(aircraft0, Aircraft0)
                thereIsAnAircraft(aircraft1, Aircraft1)
            }
        }
        then {
            hasAircraft(airspaceUK, aircraft0)
            hasAircraft(airspaceUK, aircraft1)
        }
    }
}
