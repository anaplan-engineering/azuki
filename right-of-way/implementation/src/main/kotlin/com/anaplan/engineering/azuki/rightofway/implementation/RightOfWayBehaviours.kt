package com.anaplan.engineering.azuki.rightofway.implementation

import kotlin.math.atan

//TODO LF: these are in the system objects, but not accessible here, bit ugly?
enum class QuadrantImpl { Q1, Q2, Q3, Q4 }
enum class ConvergenceImpl { Convergence, Divergence, Overtake }
enum class DirectionImpl { Same, Opposite }

interface QuadrantBehaviours {
    fun quadrant(aircraft: Aircraft, position: Position): QuadrantImpl
    fun quadrantConvergence(aircraft1: Aircraft, aircraft2: Aircraft): ConvergenceImpl

    // Delphi/Ada idiom to index on arrays by enum types to avoid complicated if-then-else-chains
    companion object {
        val CONVERGENCE_MATRICES = arrayOf(
            arrayOf(ConvergenceImpl.Convergence, ConvergenceImpl.Convergence, ConvergenceImpl.Overtake, ConvergenceImpl.Overtake),
            arrayOf(ConvergenceImpl.Convergence, ConvergenceImpl.Convergence, ConvergenceImpl.Overtake, ConvergenceImpl.Overtake),
            arrayOf(ConvergenceImpl.Overtake, ConvergenceImpl.Overtake, ConvergenceImpl.Divergence, ConvergenceImpl.Divergence),
            arrayOf(ConvergenceImpl.Overtake, ConvergenceImpl.Overtake, ConvergenceImpl.Divergence, ConvergenceImpl.Divergence),
        )
    }
}

interface OrientationBehaviours {
    fun track(aircraft: Aircraft) = atan(aircraft.velocity().x() / aircraft.velocity().y())
    fun timeToClosestPointApproach(aircraft1: Aircraft, aircraft2: Aircraft): Double
    fun horizontalMissDistance(aircraft1: Aircraft, aircraft2: Aircraft): Double

    fun orientation(aircraft1: Aircraft, aircraft2: Aircraft): Double
    fun parallel(aircraft1: Aircraft, aircraft2: Aircraft): Boolean
    fun direction(aircraft1: Aircraft, aircraft2: Aircraft): DirectionImpl
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

operator fun <T> Array<Array<T>>.get(row: QuadrantImpl, col: QuadrantImpl): T = this[row.ordinal][col.ordinal]
operator fun <T> Array<Array<T>>.set(row: QuadrantImpl, col: QuadrantImpl, value: T) { this[row.ordinal][col.ordinal] = value }



