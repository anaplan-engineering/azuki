package com.anaplan.engineering.azuki.rightofway.implementation

import com.anaplan.engineering.azuki.rightofway.implementation.QuadrantBehaviours.Companion.CONVERGENCE_MATRIX
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs

const val DELTA_O = 100.0
const val DELTA_C = 900.0
const val THETA_H = 50.0

class SampleQuadrantBehaviours : QuadrantBehaviours {
    private fun getQuadrant(aircraft: Aircraft, position: Position): Vector2 {
        val psub = position.toVector() - aircraft.position().toVector()
        val vrot = aircraft.velocity().toVector().rotate90()
        return Vector2(psub dot vrot, psub dot aircraft.velocity().toVector())
    }

    override fun quadrant(aircraft: Aircraft,position: Position): Quadrant {
        val q = getQuadrant(aircraft, position)
        return when {
            q.x > 0.0 && q.y >= 0.0 -> Quadrant.Q1
            q.x <= 0.0 && q.y > 0.0 -> Quadrant.Q2
            q.x < 0.0 && q.y <= 0.0 -> Quadrant.Q3
            q.x >= 0.0 && q.y < 0.0 -> Quadrant.Q4
            else -> throw IllegalStateException("Invalid quadrant for vector $q")
        }
    }

    override fun quadrantConvergence(aircraft1: Aircraft, aircraft2: Aircraft): Convergence {
        val a1Q = quadrant(aircraft1, aircraft2.position())
        val a2Q = quadrant(aircraft2, aircraft1.position())
        return CONVERGENCE_MATRIX[a1Q, a2Q]
    }
}

class SamplePositionBehaviours : PositionBehaviours {

    private fun relativePosition(aircraft: Aircraft, position: Position) =
        (position.toVector() - aircraft.position().toVector()) dot aircraft.velocity().toVector().rotate90()

    private fun relativeVelocity(aircraft1: Aircraft, aircraft2: Aircraft) =
        aircraft1.velocity().toVector() dot aircraft2.velocity().toVector().rotate90()

    override fun toTheLeftOf(aircraft: Aircraft, position: Position) =
        relativePosition(aircraft, position) < 0.0

    override fun toTheRightOf(aircraft: Aircraft, position: Position) =
        relativePosition(aircraft, position) > 0.0

    override fun leftToRight(aircraft1: Aircraft, aircraft2: Aircraft) =
        relativePosition(aircraft1, aircraft2.position()) < 0.0

    override fun rightToLeft(aircraft1: Aircraft, aircraft2: Aircraft) =
        relativePosition(aircraft1, aircraft2.position()) > 0.0
}

class SampleOrientationBehaviours : OrientationBehaviours {
    override fun timeToClosestPointApproach(aircraft1: Aircraft, aircraft2: Aircraft): Double {
        if (aircraft1.velocity() == aircraft2.velocity()) {
            return 0.0
        }
        else {
            val subP1P2 = aircraft1.position().toVector() - aircraft2.position().toVector()
            val subV1V2 = aircraft1.velocity().toVector() - aircraft2.velocity().toVector()
            val vDiffDot = subV1V2 dot subV1V2
            val r = - ((subP1P2 dot subV1V2) / vDiffDot)
            return r
        }
    }

    // HMD(a0, 01) = ||(a0.p - a1.p) + (a0.v - a1.v) * t||
    override fun horizontalMissDistance(aircraft1: Aircraft, aircraft2: Aircraft): Double {
        val subP1P2 = aircraft1.position().toVector() - aircraft2.position().toVector()
        val subV1V2 = aircraft1.velocity().toVector() - aircraft2.velocity().toVector()
        val sprod = subV1V2 * timeToClosestPointApproach(aircraft1, aircraft2)
        return (subP1P2 + sprod).length
    }

    override fun orientation(aircraft1: Aircraft, aircraft2: Aircraft) =
        (aircraft1.velocity().toVector() dot aircraft2.velocity().toVector())

    override fun direction(aircraft1: Aircraft, aircraft2: Aircraft): Direction {
        val ori = orientation(aircraft1, aircraft2)
        return when {
            ori < 0.0 -> Direction.Opposite
            ori > 0.0 -> Direction.Same
            else -> throw IllegalStateException("Invalid direction orientation at $ori degrees")
        }
    }

    override fun parallel(aircraft1: Aircraft, aircraft2: Aircraft) =
        (aircraft1.velocity().toVector() dot aircraft2.velocity().toVector().rotate90()) == 0.0
}

class SampleCrossingBehaviours(
    val positionBehaviours: PositionBehaviours = SamplePositionBehaviours()
) : CrossingBehaviours, PositionBehaviours by positionBehaviours {
    override fun crossing(aircraft1: Aircraft, aircraft2: Aircraft) =
        (positionBehaviours.toTheLeftOf(aircraft1, aircraft2.position()) &&
            positionBehaviours.leftToRight(aircraft1, aircraft2))
            ||
            (positionBehaviours.toTheRightOf(aircraft1, aircraft2.position()) &&
            positionBehaviours.rightToLeft(aircraft1, aircraft2))

    override fun crossed(aircraft1: Aircraft, aircraft2: Aircraft) =
        (positionBehaviours.toTheLeftOf(aircraft1, aircraft2.position()) &&
            positionBehaviours.rightToLeft(aircraft1, aircraft2))
            ||
            (positionBehaviours.toTheRightOf(aircraft1, aircraft2.position()) &&
            positionBehaviours.leftToRight(aircraft1, aircraft2))

    override fun zeroCrossed(aircraft1: Aircraft, aircraft2: Aircraft) =
        crossing(aircraft1, aircraft2) && crossing(aircraft2, aircraft1)

    override fun oneCrossed(aircraft1: Aircraft, aircraft2: Aircraft) =
        (crossing(aircraft1, aircraft2) && crossed(aircraft2, aircraft1))
            ||
            (crossing(aircraft2, aircraft1) && crossed(aircraft1, aircraft2))

    override fun bothCrossed(aircraft1: Aircraft, aircraft2: Aircraft) =
        crossed(aircraft1, aircraft2) && crossed(aircraft2, aircraft1)
}

class SampleConvergenceBehaviours(
    val quadrantBehaviours: QuadrantBehaviours = SampleQuadrantBehaviours(),
    val orientationBehaviours: OrientationBehaviours = SampleOrientationBehaviours(),
) : ConvergenceBehaviours,
    QuadrantBehaviours by quadrantBehaviours,
    OrientationBehaviours by orientationBehaviours
{
    var delta_c: Double = DELTA_C
    var Theta_h: Double = THETA_H

    override fun converging(aircraft1: Aircraft, aircraft2: Aircraft) =
        quadrantBehaviours.quadrantConvergence(aircraft1, aircraft2) == Convergence.Convergence &&
            orientationBehaviours.horizontalMissDistance(aircraft1, aircraft2) < delta_c

    override fun headon(aircraft1: Aircraft, aircraft2: Aircraft) =
        converging(aircraft1, aircraft2) &&
            abs(orientationBehaviours.track(aircraft1) - orientationBehaviours.track(aircraft2)) in
            (180.0 - Theta_h)..(180.0 + Theta_h)

    override fun convergingNotHeadon(aircraft1: Aircraft, aircraft2: Aircraft) =
        converging(aircraft1, aircraft2) &&
            abs(orientationBehaviours.track(aircraft1) - orientationBehaviours.track(aircraft2)) <
            (180.0 - Theta_h)

}

class SampleRightOfWayBehaviours(
    private val quadrantBehaviours: QuadrantBehaviours = SampleQuadrantBehaviours(),
    private val orientationBehaviours: OrientationBehaviours = SampleOrientationBehaviours(),
    private val positionBehaviours: PositionBehaviours = SamplePositionBehaviours(),
    private val crossingBehaviours: CrossingBehaviours = SampleCrossingBehaviours(),
    //TODO not sure here, becuase needs to pass through the delta_c and Theta_h through
    private val convergenceBehaviours: ConvergenceBehaviours = SampleConvergenceBehaviours(),
    val delta_c: Double = DELTA_C,
    val Theta_h: Double = THETA_H,
    val delta_o: Double = DELTA_O,
) : RightOfWayBehaviours,
    QuadrantBehaviours by quadrantBehaviours,
    OrientationBehaviours by orientationBehaviours,
    PositionBehaviours by positionBehaviours,
    CrossingBehaviours by crossingBehaviours,
    ConvergenceBehaviours by convergenceBehaviours {

    //TODO what's best practice here? Have multiple interfaces or "force" this one specific?
//    val positionBehaviours: PositionBehaviours = (crossingBehaviours as SampleCrossingBehaviours).positionBehaviours
//    val quadrantBehaviours: QuadrantBehaviours = (convergenceBehaviours as SampleConvergenceBehaviours).quadrantBehaviours
//    val orientationBehaviours: OrientationBehaviours = (convergenceBehaviours as SampleConvergenceBehaviours).orientationBehaviours

    init {
        require(delta_c > 0) { "delta_c must be positive, got $delta_c" }
        require(Theta_h > 0) { "Theta_h must be positive, got $Theta_h" }
        require(delta_o > 0) { "delta_o must be positive, got $delta_o" }

        //TODO this looks ugly :-(. Wasn't sure how to "pass through" fields in composite interfaces chaining
        (convergenceBehaviours as SampleConvergenceBehaviours).delta_c = delta_c
        (convergenceBehaviours as SampleConvergenceBehaviours).Theta_h = Theta_h
    }

    override fun overtaking(aircraft1: Aircraft, aircraft2: Aircraft) =
        // won't work given it's not a class inheritance
        quadrantBehaviours.quadrant(aircraft1, aircraft2.position()) in setOf(Quadrant.Q1, Quadrant.Q2) &&
            quadrantBehaviours.quadrant(aircraft2, aircraft1.position()) in setOf(Quadrant.Q3, Quadrant.Q4) &&
            orientationBehaviours.horizontalMissDistance(aircraft1, aircraft2) < delta_o

    override fun hasRightOfWay(aircraft1: Aircraft, aircraft2: Aircraft) =
        overtaking(aircraft1, aircraft2) ||
            //TODO needs initialiser for passing the constant airspace parameters; didn't want to have it as API parameters
            (convergenceBehaviours.convergingNotHeadon(aircraft1, aircraft2) &&
                positionBehaviours.toTheRightOf(aircraft1, aircraft2.position()) &&
                crossingBehaviours.zeroCrossed(aircraft1, aircraft2))
}

class AirspaceV1 internal constructor(
    state: AirspaceState,
    // have behavioural interfaces here to allow for composite implementations
//    private val quandrantBehaviours: QuadrantBehaviours = SampleQuadrantBehaviours(),
//    private val positionBehaviours: PositionBehaviours = SamplePositionBehaviours(),
//    private val orientationBehaviours: OrientationBehaviours = SampleOrientationBehaviours(),
//    private val crossingBehaviours: CrossingBehaviours = SampleCrossingBehaviours(),
//    private val convergenceBehaviours: ConvergenceBehaviours = SampleConvergenceBehaviours(),
//    private val rightOfWayBehaviours: RightOfWayBehaviours = SampleRightOfWayBehaviours(),
    val behaviours: SampleRightOfWayBehaviours
) : Airspace(state),
    QuadrantBehaviours by behaviours,
    PositionBehaviours by behaviours,
    OrientationBehaviours by behaviours,
    CrossingBehaviours by behaviours,
    ConvergenceBehaviours by behaviours,
    RightOfWayBehaviours by behaviours
{

    constructor(state: AirspaceState) :
        this(state, SampleRightOfWayBehaviours(
            delta_c = state.delta_c, Theta_h = state.Theta_h, delta_o = state.delta_o))
    constructor(prepopulated: AircraftData = emptyMap()) :
        this(AirspaceState(DELTA_O, DELTA_C, THETA_H, prepopulated.toMutableMap()),
            SampleRightOfWayBehaviours(
                SampleQuadrantBehaviours(),
                SampleOrientationBehaviours(),
                SamplePositionBehaviours(),
               SampleCrossingBehaviours(),
                SampleConvergenceBehaviours(), DELTA_C, THETA_H, DELTA_O)
        )


    override val log: Logger = LoggerFactory.getLogger(AirspaceV1::class.java)

    /*
        val isInQ1andWasInQ2 = function(
            command = { a0: Aircraft, a1: Aircraft ->
                (Q1(a0, a1.position) && Q1(a1, a0.position))
                    ||
                    (opposite_orientation(a0, a1) && left_to_right(a0, a1));
            }
        )

     */


}

class AirspaceV1Creator : AirspaceCreator {

    override fun create(state: AirspaceState) = AirspaceV1(state)

    override fun create(prepopulated: AircraftData) = AirspaceV1(prepopulated)

}

// TODO Varying imoplementation say on vector functionalities?
fun Pair<Double, Double>.toVector(): Vector2 = Vector2(first, second)
fun Vector2.toPair(): Pair<Double, Double> = Pair(x, y)

data class Vector2(val x: Double, val y: Double) {
    // Plus Operator: v1 + v2
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    // Minus Operator: v1 - v2
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    // Scalar Multiplication (Vector * Scalar): v1 * 2.0
    operator fun times(scalar: Double) = Vector2(x * scalar, y * scalar)

    // Scalar Division (Vector / Scalar): v1 / 2.0
    operator fun div(scalar: Double) = Vector2(x / scalar, y / scalar)

    // Dot Product: v1 dot v2
    infix fun dot(other: Vector2): Double = (x * other.x) + (y * other.y)

    fun rotate90() = Vector2(y, -x)

    // Vector Length/Magnitude
    val length: Double get() = kotlin.math.sqrt((x * x) + (y * y))

    override fun toString(): String = "Vector2(x=$x, y=$y)"
}
