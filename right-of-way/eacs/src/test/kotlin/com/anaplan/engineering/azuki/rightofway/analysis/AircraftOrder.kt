package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.runner.ToBeDone
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayWhen
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.adapter.api.aircraft
import com.anaplan.engineering.azuki.rightofway.position0
import com.anaplan.engineering.azuki.rightofway.position1
import com.anaplan.engineering.azuki.rightofway.velocity0
import com.anaplan.engineering.azuki.rightofway.velocity1
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
//            testCase("a0") {
//                placeAircraft(airspaceUK, aircraft0, Aircraft0)
//            },
            testCase("a0 + a1") {
                placeAircraft(airspaceUK, a0, aircraft(position0 to velocity0))
                placeAircraft(airspaceUK, a1, aircraft(position1 to velocity1))
            },
            testCase("a0 + a0", KnownBug(Issue("SampleImpl", "FOO-123"), Issue("VDM", "FOO-234"))) {
                placeAircraft(airspaceUK, a0, aircraft(position0 to velocity0))
                placeAircraft(airspaceUK, a0, aircraft(position0 to velocity0))
            },
            testCase("a1 + a0", toBeDone = ToBeDone(Issue("SampleImpl", "BAR-567"))) {
                placeAircraft(airspaceUK, a1, aircraft(position1 to velocity1))
                placeAircraft(airspaceUK, a0, aircraft(position0 to velocity0))
            }
        )
    }

//    @AnalysisScenario
    fun equivalentAircraftOrder() {
        val testCase = this.testCase
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        whenever {
            testCase.moves(this)
        }
        then {
            hasAircraft(airspaceUK, a0)
            hasAircraft(airspaceUK, a1)
        }
    }

    fun foo() {
        given {
            thereIsAnAirspace(airspaceUK) {
                // add to the DSL to build the aircraft
                thereIsAnAircraft(a0, aircraft(position0 to velocity0))
                thereIsAnAircraft(a1, aircraft(position1 to velocity1))
            }
        }
        then {
            hasAircraft(airspaceUK, a0)
            hasAircraft(airspaceUK, a1)
        }
    }
}
