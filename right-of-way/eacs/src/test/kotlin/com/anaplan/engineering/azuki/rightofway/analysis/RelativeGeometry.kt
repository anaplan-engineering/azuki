package com.anaplan.engineering.azuki.rightofway.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.rightofway.a0
import com.anaplan.engineering.azuki.rightofway.a0a1Airspace
import com.anaplan.engineering.azuki.rightofway.convergingAirspace
import com.anaplan.engineering.azuki.rightofway.headOnAirspace
import com.anaplan.engineering.azuki.rightofway.a1
import com.anaplan.engineering.azuki.rightofway.adapter.api.Quadrant
import com.anaplan.engineering.azuki.rightofway.airspaceUK
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRunnableScenario

class RelativeGeometry : RightOfWayRunnableScenario() {

    @AnalysisScenario
    fun defaultPairOrientation() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            hasOrientation(airspaceUK, a0, a1, same = true)
        }
    }

    @AnalysisScenario
    fun defaultPairConvergence() {
        given {
            thereIsAnAirspace(airspaceUK, convergingAirspace)
        }
        then {
            isConverging(airspaceUK, a0, a1)
            hasQuadrantConvergence(airspaceUK, a0, a1)
        }
    }

    @AnalysisScenario
    fun defaultPairHeadOn() {
        given {
            thereIsAnAirspace(airspaceUK, headOnAirspace, opened = false)
        }
        then {
            isHeadOn(airspaceUK, a0, a1)
        }
    }

    @AnalysisScenario
    fun defaultPairOvertaking() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            isOvertaking(airspaceUK, a0, a1)
        }
    }

    @AnalysisScenario
    fun defaultPairCrossing() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            isGoingToCross(airspaceUK, a0, a1)
            hasZeroCrossed(airspaceUK, a0, a1)
            hasOneCrossed(airspaceUK, a0, a1)
            hasBothCrossed(airspaceUK, a0, a1)
            hasCrossed(airspaceUK, a0, a1)
        }
    }

    @AnalysisScenario
    fun defaultPairHorizontalMissDistance() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            horizontalMissDistanceIs(airspaceUK, a0, a1, hmd = 45.254834)
        }
    }

    @AnalysisScenario
    fun defaultPairTimeToClosestPointApproach() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            timeToClosestPointApproachIs(airspaceUK, a0, a1, tcpa = -5.0)
        }
    }

    @AnalysisScenario
    fun defaultPairQuadrant() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            onQuadrantRelativeTo(airspaceUK, a0, a1, Quadrant.FRONT_LEFT)
        }
    }

    @AnalysisScenario
    fun defaultPairOnTrack() {
        given {
            thereIsAnAirspace(airspaceUK, a0a1Airspace)
        }
        then {
            onTrack(airspaceUK, a0, angle = 63.43494882307885)
        }
    }
}
