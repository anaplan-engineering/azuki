package com.anaplan.engineering.azuki.rightofway.implementation

import kotlin.math.atan

enum class Convergence { Convergence, Divergence, Overtake }
enum class Quadrant { Q1, Q2, Q3, Q4 }
enum class Direction { Same, Opposite }

interface QuadrantBehaviours {
    fun quadrant(aircraft: Aircraft, position: Position): Quadrant
    fun quadrantConvergence(aircraft1: Aircraft, aircraft2: Aircraft): Convergence

    companion object {
        val CONVERGENCE_MATRIX = arrayOf(
            arrayOf(Convergence.Convergence, Convergence.Convergence, Convergence.Overtake, Convergence.Overtake),
            arrayOf(Convergence.Convergence, Convergence.Convergence, Convergence.Overtake, Convergence.Overtake),
            arrayOf(Convergence.Overtake, Convergence.Overtake, Convergence.Divergence, Convergence.Divergence),
            arrayOf(Convergence.Overtake, Convergence.Overtake, Convergence.Divergence, Convergence.Divergence),
        )
    }
}

interface OrientationBehaviours {
    fun track(aircraft: Aircraft) = atan(aircraft.velocity().x() / aircraft.velocity().y())
    fun timeToClosestPointApproach(aircraft1: Aircraft, aircraft2: Aircraft): Double
    fun horizontalMissDistance(aircraft1: Aircraft, aircraft2: Aircraft): Double

    fun orientation(aircraft1: Aircraft, aircraft2: Aircraft): Double
    fun parallel(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun direction(aircraft1: Aircraft, aircraft2: Aircraft): Direction
}

interface PositionBehaviours {
    fun toTheLeftOf(aircraft: Aircraft, position: Position): Boolean
    fun toTheRightOf(aircraft: Aircraft, position: Position): Boolean
    fun leftToRight(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun rightToLeft(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
}

interface CrossingBehaviours {
    fun crossing(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun crossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun zeroCrossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun oneCrossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun bothCrossed(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
}

interface ConvergenceBehaviours {
    fun converging(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun headon(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun convergingNotHeadon(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
}

interface RightOfWayBehaviours {
    fun overtaking(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun hasRightOfWay(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
}

operator fun <T> Array<Array<T>>.get(row: Quadrant, col: Quadrant): T = this[row.ordinal][col.ordinal]
operator fun <T> Array<Array<T>>.set(row: Quadrant, col: Quadrant, value: T) { this[row.ordinal][col.ordinal] = value }



