package com.anaplan.engineering.azuki.rightofway.kazuki

import com.anaplan.engineering.azuki.rightofway.kazuki.Position_Module.as_Position
import com.anaplan.engineering.azuki.rightofway.kazuki.RVector_Module.mk_RVector
//import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay.Companion.rightOfWay
import com.anaplan.engineering.azuki.rightofway.kazuki.Velocity_Module.as_Velocity
import com.anaplan.engineering.kazuki.core.*
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.sqrt

const val DEFAULT_SQRT_ERROR: PNZReal = 0.000001;

@PrimitiveInvariant(name = "Real", base = Double::class)
fun isReal(r: Double) = !r.isNaN() && !r.isInfinite()

@PrimitiveInvariant(name = "PReal", base = Double::class)
fun isPReal(r: Double) = isReal(r) && r >= 0.0

@PrimitiveInvariant(name = "NZReal", base = Double::class)
fun isNZReal(r: Double) = isReal(r) && r != 0.0

@PrimitiveInvariant(name = "NReal", base = Double::class)
fun isNReal(r: Double) = isReal(r) && r < 0.0

//    @PrimitiveInvariant(name = "PNZReal", base = PReal::class)
@PrimitiveInvariant(name = "PNZReal", base = Double::class)
fun isPNZReal(r: Double) = isPReal(r) && isNZReal(r)

@PrimitiveInvariant(name = "NNZReal", base = Double::class)
fun isNNZReal(r: Double) = isNReal(r) && isNZReal(r)

@PrimitiveInvariant(name = "Angle", base = Double::class)
fun isAngle(r: Double) = isPReal(r) && r <= 360.0

@Module
@ComparableTypeLimit
interface RVector {
//    val x: Real
//    val y: Real
//    [ksp] java.lang.IllegalArgumentException: Error type '<ERROR TYPE: Real>' is not resolvable in the current round of processing.
//    at com.squareup.kotlinpoet.ksp.KsTypesKt.toTypeName(KsTypes.kt:61)

    val x: Double
    val y: Double

    fun isReal() = isReal(x) && isReal(y)
}

// aircraft position
@Module
interface Position : RVector {
    @Invariant
    fun isPReal() = isPReal(x) && isPReal(y)
}

// aircraft velocity
@Module
interface Velocity : RVector {
    @Invariant
    fun isPNZReal() = isPNZReal(x) && isPNZReal(y)
}

@Module
interface Aircraft {
    val position: Position
    val velocity: Velocity
}

@Module
interface ConvergingAircraft {
    val a0: Aircraft
    val a1: Aircraft
    val delta_c: PReal

//    @Invariant
//    fun converges() = (a0 != a1) implies { rightOfWay.converging(a0, a1)(delta_c) }
}

@Module
interface AirSpace {

    val aircrafts: Set<Aircraft>

    @Invariant
    fun uniquePositions() = properties.allPositions.card == aircrafts.card

    @Invariant
    fun noHeadOn() = forall(aircrafts) {
        a -> forall(aircrafts) {
            b -> (a != b) implies {
//                !functions.headon(a, b)(properties.delta_c, properties.Theta_h)
                true
            }
        }
    }

    @FunctionProvider(AirspaceProperties::class)
    val properties: AirspaceProperties

//    FunctionProvider(RightOfWay::class)
//    val functions: RightOfWay
}

class AirspaceProperties(private val airspace: AirSpace) {
    val delta_o by property { 1.0 }
    val delta_c by property { 2.0 }
    val Theta_h by property { 170.0 }
    val allPositions by property { airspace.aircrafts.map { it.position }.toSet() }
}

//TODO Can this be just an object in that sense ?
@Module
object RightOfWay {
//class RightOfWay(private val airspace: AirSpace) {
//
//    companion object {
//        val rightOfWay = RightOfWay()
//    }

    val sumVectors = function(
        command = { u: RVector, v: RVector ->
            mk_RVector(u.x + v.x, u.y + v.y)
        }
    )

    val minusVector = function(
        command = { u: RVector ->
            mk_RVector(-u.x, -u.y)
        }
    )

    val subtractVectors = function(
        command = { u: RVector, v: RVector ->
            sumVectors(u, minusVector(v))
        }
    )

    val rotate90 = function(
        command = { u: RVector ->
            mk_RVector(u.y, -u.x)
        }
    )

    val isZeroVector = function(
        command = { u: RVector ->
            u.x == 0.0 && u.y == 0.0
        }
    )

    val dot_product = function(
        command = { u: RVector, v: RVector ->
            if (isZeroVector(u) || isZeroVector(v))  0.0
            else if (u.y == 0.0 || v.y == 0.0) u.x * v.x
            else if (u.x == 0.0 || v.x == 0.0) u.y * v.y
            else u.x * v.x + u.y * v.y
        },
        // no need for pre given the invariant of RVector?
        pre = { u, v -> u.isReal() && v.isReal() },
        post = { u, v, r ->
            (isZeroVector(u) && isZeroVector(v) implies (r == 0.0))
                &&
            (u == v && !isZeroVector(u) implies isNZReal(r))
                &&
            // (u . v)^2 <= (u . u) * (v . v)
            (u.x*v.x + u.y*v.y) * (u.x*v.x + u.y*v.y) <=
            (u.x*u.x + u.y*u.y) * (v.x*v.x + v.y*v.y)
        }
    )

    val scalar_product = function(
        command = { x: Double, u: RVector ->
            mk_RVector(x * u.x, x * u.y )
        }
    )

    val magnitude = function(
        command = { u: RVector ->
            sqrt(u.x * u.x + u.y * u.y)
            //sqrt'(u.x * u.x + u.y * u.y, DEFAULT_SQRT_ERROR)
        }
    )

    //TODO when to use a function like this or an @FunctionProvider??

    // Aircraft quadrant relative to plane
    // 1: front right
    // 2: front left
    // 3: behind left
    // 4: behind right
    val Q1 = function(
        command = { a: Aircraft, p: Position ->
            // refactor to avoid repetition
            //val psub = subtractVectors(p, a.position) as Position
            val psub = as_Position(subtractVectors(p, a.position))
            val vrot = as_Velocity(rotate90(a.velocity))
            dot_product(psub, vrot) > 0.0
                &&
                dot_product(psub, a.velocity) >= 0.0
        },
        post = { a, p, r ->
            r implies to_the_right_of(a, p)
        }
    )

    val Q2 = function(
        command = { a: Aircraft, p: Position ->
            val psub = as_Position(subtractVectors(p, a.position))
            val vrot = as_Velocity(rotate90(a.velocity))
            dot_product(psub, vrot) <= 0.0
                &&
                dot_product(psub, a.velocity) > 0.0

        }
    )

    val Q3 = function(
        command = { a: Aircraft, p: Position ->
            val psub = as_Position(subtractVectors(p, a.position))
            val vrot = as_Velocity(rotate90(a.velocity))
            dot_product(psub, vrot) < 0.0
                &&
                dot_product(psub, a.velocity) <= 0.0
        }
    )

    val Q4 = function(
        command = { a: Aircraft, p: Position ->
            val psub = as_Position(subtractVectors(p, a.position))
            val vrot = as_Velocity(rotate90(a.velocity))
            dot_product(psub, vrot) >= 0.0
                &&
                dot_product(psub, a.velocity) < 0.0
        }
    )

    // Aircrafrt track (i.e. angle between north and aircraft directon)
    val track = function(
        command = { a: Aircraft ->
            // a.velocity.y is NZReal
            //TODO: how to "cast" the result type to impose invariant?
            atan(a.velocity.x / a.velocity.y) as Angle
        },
        post = { _, r -> isAngle(r) }
    )

    // Time to Closest Point of Approach
    val tCPA = function( //: VFunction2<RightOfWay.Aircraft, RightOfWay.Aircraft, out Double>
        command = { a0: Aircraft, a1: Aircraft ->
            if (a0.velocity == a1.velocity)
                0.0 // if 0, type gets captured as Number!!!!
            else {
                val pDiff = as_Position(subtractVectors(a0.position, a1.position))
                val vDiff = as_Velocity(subtractVectors(a0.velocity, a1.velocity))
                // Because Velocity are different, then their difference is not zero
                val vDiffProd = dot_product(vDiff, vDiff)
                (- ( dot_product(pDiff, vDiff) / vDiffProd)) as Double // why isn't result double? as Double
            }
        },
        post = { _, _, r -> isNZReal(r) }
    )

    // Horizontal Miss Distance is the distance at the Closest Point of Approach
    // when their position and velocity vectors are projected in time.
    val horizontalMissDistance = function(
        command = { a0: Aircraft, a1: Aircraft ->
            magnitude(
                sumVectors(
                    subtractVectors(a0.position, a1.position),
                    scalar_product(
                        tCPA(a0, a1),
                        subtractVectors(a0.velocity, a1.velocity)
                    )
                )
            )
        }
    )

    // Aircraft relative position
    val to_the_left_of = function(
        command = { a: Aircraft, p: Position ->
            dot_product(
                subtractVectors(p, a.position),
                rotate90(a.velocity)) < 0.0
        }
    )

    val to_the_right_of = function(
        command = { a: Aircraft, p: Position ->
            dot_product(
                subtractVectors(p, a.position),
                rotate90(a.velocity)) > 0.0
        }
    )

    // Aircraft relative velocity/motion
    val left_to_right = function(
        command = { a0: Aircraft, a1: Aircraft ->
            dot_product(a0.velocity, a1.velocity) < 0.0
        }
    )

    val right_to_left = function(
        command = { a0: Aircraft, a1: Aircraft ->
            dot_product(a0.velocity, a1.velocity) > 0.0
        }
    )

    // Check trajectories will cross in future
    val going_to_cross = function(
        command = { a0: Aircraft, a1: Aircraft ->
            to_the_left_of(a0, a1.position) && left_to_right(a0, a1)
                ||
                to_the_right_of(a0, a1.position) && right_to_left(a0, a1)
        }
    )

    // Check trajectories have already crossed
    val crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            to_the_left_of(a0, a1.position) && right_to_left(a0, a1)
                ||
                to_the_right_of(a0, a1.position) && left_to_right(a0, a1)
        }
    )

    // Check trajectories are going to cross but have not yet crossed
    val zero_crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            going_to_cross(a0, a1) && going_to_cross(a1, a0)
        }
    )

    // One aircraft (a0) has crossed trajectory of other (a1) but not other way round
    val one_crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            going_to_cross(a0, a1) && crossed(a0, a1)
                ||
                going_to_cross(a1, a0) && crossed(a1, a0)
        }
    )

    // both aircraft crossed each other's trajectories
    val both_crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            crossed(a0, a1) && crossed(a1, a0)
        }
    )

    val orientation = function(
        command = { a0: Aircraft, a1: Aircraft ->
            dot_product(a0.velocity, rotate90(a1.velocity)) //as Angle
        },
        //post = { _, _, r -> isAngle(r) }
    )
    val parallel = function(
        command = { a0: Aircraft, a1: Aircraft ->
            orientation(a0, a1) == 0.0
        }
    )

    val same_orientation = function(
        command = { a0: Aircraft, a1: Aircraft ->
            orientation(a0, a1) > 0.0
        }
    )

    val opposite_orientation = function(
        command = { a0: Aircraft, a1: Aircraft ->
            orientation(a0, a1) < 0.0
        }
    )

    val isInQ1andWasInQ2 = function(
        command = { a0: Aircraft, a1: Aircraft ->
            Q1(a0, a1.position) && Q1(a1, a0.position)
                ||
                (opposite_orientation(a0, a1) and left_to_right(a0, a1));
        }
    )

    // Quadrant convergence
    val QC = function(
        command = { a0: Aircraft, a1: Aircraft ->
            Q1(a0, a1.position) && Q1(a1, a0.position)
                ||
                Q2(a0, a1.position) && Q2(a1, a0.position)
                ||
                Q3(a0, a1.position) && Q3(a1, a0.position)
                ||
                Q4(a0, a1.position) && Q4(a1, a0.position)
        }
    )

    val converging = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_c: PReal ->
                    QC(a0, a1) &&
                        horizontalMissDistance(a0, a1) < delta_c
                }
            )
            inner
        }
    )

    val conv_not_headon = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_c: PReal, Theta_h: Angle ->
                    val track_delta = abs(track(a0) - track(a1))
                    converging(a0, a1)(delta_c) &&
                        (180.0 + Theta_h < track_delta)
                        ||
                        (track_delta < 180.0 - Theta_h)
                }
            )
            inner
        }
    )

    val headon = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_c: PReal, Theta_h: Angle ->
                    val track_delta = abs(track(a0) - track(a1))
                    converging(a0, a1)(delta_c) &&
                        (180.0 + Theta_h <= track_delta)
                        ||
                        (track_delta < 180.0 + Theta_h)
                }
            )
            inner
        }
    )

    val overtaking = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_o: Real ->
                    Q1(a0, a1.position) || Q2(a0, a1.position)
                        &&
                    Q3(a1, a0.position) || Q4(a1, a0.position)
                        &&
                        horizontalMissDistance(a0, a1) < delta_o
                }
            )
            inner
        }
    )

    val right_of_way = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_o: Real, delta_c: PReal, Theta_h: Angle ->
                    overtaking(a0, a1)(delta_o)
                        ||
                       ( conv_not_headon(a0, a1)(delta_c, Theta_h)
                        &&
                        to_the_right_of(a0, a1.position)
                        &&
                        zero_crossed(a0, a1))
                }
            )
            inner
        }
    )
}

