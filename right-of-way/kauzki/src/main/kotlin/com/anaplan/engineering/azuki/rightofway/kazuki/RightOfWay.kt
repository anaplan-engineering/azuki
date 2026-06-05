package com.anaplan.engineering.azuki.rightofway.kazuki

import com.anaplan.engineering.azuki.rightofway.kazuki.Position_Module.as_Position
import com.anaplan.engineering.azuki.rightofway.kazuki.Position_Module.is_Position
import com.anaplan.engineering.azuki.rightofway.kazuki.RVector_Module.mk_RVector
//import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay.Companion.rightOfWay
import com.anaplan.engineering.azuki.rightofway.kazuki.Velocity_Module.as_Velocity
import com.anaplan.engineering.azuki.rightofway.kazuki.Velocity_Module.is_Velocity
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
interface AirSpace {

    val aircrafts: Set<Aircraft>

    @Invariant
    fun uniquePositions() = properties.allPositions.card == aircrafts.card

    @Invariant
    fun noHeadOn() = functions.noHeadOn()

    @Invariant
    fun safeAirspace() = forall(aircrafts) { a0 ->
        forall( aircrafts - {a0} ) { a1 ->
            // note that different from VDM-SL, thmX definitions uses properties directly (e.g., akin to how would be in VDM-PP)
            functions.thm1_determnistic_rw(a0, a1) &&
            functions.thm2_zero_crossed_only_one_to_right(a0, a1) &&
            functions.thm3_overtaking_asymmetric(a0, a1) &&
            functions.thm4_no_rw_after_crossing(a0, a1) &&
            functions.thm5_mutual_awareness(a0, a1) &&
            functions.thm6_had_rw(a0, a1)
        }
    }

    @FunctionProvider(AirspaceProperties::class)
    val properties: AirspaceProperties

    @FunctionProvider(RightOfWay::class)
    val functions: RightOfWay
}

class AirspaceProperties(private val airspace: AirSpace) {
    val delta_o by property { 1.0 }
    val delta_c by property { 2.0 }
    val Theta_h by property { 150.0 }
    val allPositions by property { airspace.aircrafts.map { it.position }.toSet() }
    val allVelocities by property { airspace.aircrafts.map { it.velocity }.toSet() }
}

/**
 * Right of way functions. Could have been a @Module object RightOfWay directly
 * Wanted to explore other aspects of Azuki/Kazuki linkage
 */
//TODO add @ComparableProperty, @ComparableTypeLimit etc.
class RightOfWay(private val airspace: AirSpace) {

    // Arguably refactor this out to RVector?
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
        //TODO remove? no need for pre given the invariant of RVector?
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

    // Aircraft quadrant relative to plane
    // 1: front right
    // 2: front left
    // 3: behind left
    // 4: behind right
    //TODO refactor the repetition below x keep closer to paper's definitions
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
            //TODO do we need the casting here? imposes the resulting subtraction etc carries the type
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
        },
        post = { a, p, r ->
            r implies to_the_left_of(a, p)
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

    // Aircraft track (i.e., angle between north and aircraft direction)
    val track = function(
        command = { a: Aircraft ->
            // a.velocity.y is NZReal
            //TODO: how to "cast" the result type to impose invariant?
            atan(a.velocity.x / a.velocity.y) as Angle
        },
        pre = { a -> isNZReal(a.velocity.y) },
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
                // TODO how to `cast` result to NZReal?
                val vDiffProd = dot_product(vDiff, vDiff) as NZReal
                (- ( dot_product(pDiff, vDiff) / vDiffProd)) as Double // why isn't result double? as Double
            }
        },
        // example where internal typing constraints matter
        // TODO this could be simplified with invariants (or is already)?
        pre = { a0, a1 ->
            (a0.velocity != a1.velocity) implies {
                val vDiff = subtractVectors(a0.velocity, a1.velocity)
                val vDiffProd = dot_product(vDiff, vDiff)
                is_Position(subtractVectors(a0.position, a1.position))
                    &&
                is_Velocity(vDiff)
                    &&
                    isNZReal(vDiffProd)
            }
        },
        post = { a0, a1, r -> (a0.velocity != a1.velocity) implies isNNZReal(r) }
    )

    // Horizontal Miss Distance (HMD) is the distance at the Closest Point of Approach
    // when their position and velocity vectors are projected in time. Assumes delta_c ~< 1000 nautical miles
    //
    // case 1(HMD > delta_c): a0 travelling west, a1 travelling south BEFORE a0 HMD
    //                                                     |
    //                                                    \/
    //                                                    a1 [a1 travelling south before]
    // a0 ->----------------HMD--------------------------->  [a0 travelling west ]
    //
    // case 2(HMD <= delta_c): a0 travelling west, a1 travelling south AFTER a0 HMD
    //                                                     |
    //                                                    \/
    // a0 ->----------------HMD--------------------------->
    //                                                    a1 [a1 after a0]
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

    // aircraft relative position (i.e., p is to the left of a's position)
    val to_the_left_of = function(
        command = { a: Aircraft, p: Position ->
            dot_product(
                subtractVectors(p, a.position),
                rotate90(a.velocity)) < 0.0
        }
    )

    // aircraft relative position (i.e., p is to the right of a's position)
    val to_the_right_of = function(
        command = { a: Aircraft, p: Position ->
            dot_product(
                subtractVectors(p, a.position),
                rotate90(a.velocity)) > 0.0
        }
    )

    // aircraft relative motion (i.e., a1 is moving left to right relative to a0)
    val left_to_right = function(
        command = { a0: Aircraft, a1: Aircraft ->
            dot_product(a0.velocity, rotate90(a1.velocity)) < 0.0
        }
    )

    // aircraft relative motion (i.e., a1 is moving right to left relative to a0)
    val right_to_left = function(
        command = { a0: Aircraft, a1: Aircraft ->
            dot_product(a0.velocity, rotate90(a1.velocity)) > 0.0
        }
    )

    // check trajectories will cross in future (i.e., a1 is going to cross a0's trajectory)
    val going_to_cross = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (to_the_left_of(a0, a1.position) && left_to_right(a0, a1))
                ||
                (to_the_right_of(a0, a1.position) && right_to_left(a0, a1))
        }
    )

    // check trajectories have already crossed (i.e. a1 has crossed a0's trajectory)
    val crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (to_the_left_of(a0, a1.position) && right_to_left(a0, a1))
                ||
                (to_the_right_of(a0, a1.position) && left_to_right(a0, a1))
        }
    )

    // check trajectories are going to cross but have not yet crossed
    val zero_crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            going_to_cross(a0, a1) && going_to_cross(a1, a0)
        }
    )

    // one aircraft (a0) has crossed trajectory of other (a1) but not other way round
    val one_crossed = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (going_to_cross(a0, a1) && crossed(a0, a1))
                ||
               (going_to_cross(a1, a0) && crossed(a1, a0))
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
            dot_product(a0.velocity, a1.velocity) //as Angle
        },
        //post = { _, _, r -> isAngle(r) }
    )

    val parallel = function(
        command = { a0: Aircraft, a1: Aircraft ->
            dot_product(a0.velocity, rotate90(a1.velocity)) == 0.0
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
            (Q1(a0, a1.position) && Q1(a1, a0.position))
                ||
                (opposite_orientation(a0, a1) && left_to_right(a0, a1));
        }
    )

    // Quadrant convergence
    val QC = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (Q1(a0, a1.position) && Q1(a1, a0.position))
                ||
                (Q2(a0, a1.position) && Q1(a1, a0.position))
                ||
                (Q2(a0, a1.position) && Q1(a1, a0.position))
                ||
                (Q2(a0, a1.position) && Q2(a1, a0.position))
        }
    )

    // Quadrant divergence
    val QD = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (Q3(a0, a1.position) && Q3(a1, a0.position))
                ||
                (Q3(a0, a1.position) && Q4(a1, a0.position))
                ||
                (Q4(a0, a1.position) && Q3(a1, a0.position))
                ||
                (Q4(a0, a1.position) && Q4(a1, a0.position))
        }
    )

    // Quadrant overtake
    val QO = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (Q1(a0, a1.position) && Q3(a1, a0.position))
                ||
                (Q1(a0, a1.position) && Q4(a1, a0.position))
                ||
                (Q2(a0, a1.position) && Q3(a1, a0.position))
                ||
                (Q2(a0, a1.position) && Q4(a1, a0.position))
                ||
                (Q3(a0, a1.position) && Q1(a1, a0.position))
                ||
                (Q3(a0, a1.position) && Q2(a1, a0.position))
                ||
                (Q4(a0, a1.position) && Q1(a1, a0.position))
                ||
                (Q4(a0, a1.position) && Q2(a1, a0.position))
        }
    )

    // convergence relative to quadrant convergence and horizontal miss distance up to a delta_c >= 0 threshold
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

    //  Convergence not head on bar the Theta_h angular threshold (i.e. not head on if their tracks are not within Theta_h of 180 degrees apart)
    val conv_not_headon = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_c: PReal, Theta_h: Angle ->
                    //TODO how to cast it to Angle?
                    val track_delta = abs(track(a0) - track(a1)) as Angle
                    converging(a0, a1)(delta_c) &&
                        (180.0 + Theta_h < track_delta)
                        ||
                        (track_delta < 180.0 - Theta_h)
                }
            )
            inner
        },
        pre = { a0, a1 -> isAngle(abs(track(a0) - track(a1))) }
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
        },
        pre = { a0, a1 -> isAngle(abs(track(a0) - track(a1))) }
    )

    // Aircraft a0 is overtaking a1 when a1 is in a0's Q1 or Q2 and a0 is in a1's Q3 or Q4 and
    // their horizontal miss distance is less than overtaking threshold, delta_o >= 0.0
    val overtaking = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_o: PReal ->
                    (Q1(a0, a1.position) || Q2(a0, a1.position))
                        &&
                        (Q3(a1, a0.position) || Q4(a1, a0.position))
                        &&
                        (horizontalMissDistance(a0, a1) < delta_o)
                }
            )
            inner
        }
    )

    // Aircraft a1 has right of way over a0 (which must give way), when either
    // a0 is overtaking a1, or a0 is converging (except head on) and a1 is to the
    // right of a0 and their trajectories have not yet crossed
    val right_of_way = function(
        command = { a0: Aircraft, a1: Aircraft ->
            val inner = function(
                command = { delta_o: Real, delta_c: PReal, Theta_h: Angle ->
                    overtaking(a0, a1)(delta_o)
                        ||
                       (conv_not_headon(a0, a1)(delta_c, Theta_h)
                        &&
                        to_the_right_of(a0, a1.position)
                        &&
                        zero_crossed(a0, a1))
                }
            )
            inner
        }
    )

    val noHeadOn = function(
        command = { -> true
            forall(airspace.aircrafts) {
                a -> forall(airspace.aircrafts) {
                    b -> (a != b) implies {
                        !headon(a, b)(airspace.properties.delta_c, airspace.properties.Theta_h)
                    }
                }
            }
        }
    )

    val thm1_determnistic_rw = function(
        command = { a0: Aircraft, a1: Aircraft ->
            right_of_way(a0, a1)(airspace.properties.delta_o,
                airspace.properties.delta_c, airspace.properties.Theta_h) implies {
                    !right_of_way(a1, a0)(airspace.properties.delta_o,
                        airspace.properties.delta_c, airspace.properties.Theta_h)
            }
        }
    )

    val thm2_zero_crossed_only_one_to_right = function(
        command = { a0: Aircraft, a1: Aircraft ->
            zero_crossed(a0, a1) implies {
                (to_the_right_of(a0, a1.position) && !to_the_right_of(a1, a0.position))
                    ||
                    (to_the_right_of(a1, a0.position) && !to_the_right_of(a0, a1.position))
            }
        }
    )

    val thm3_overtaking_asymmetric = function(
        command = { a0: Aircraft, a1: Aircraft ->
            overtaking(a0, a1)(airspace.properties.delta_o) implies {
                !overtaking(a1, a0)(airspace.properties.delta_o)
            }
        }
    )

    val thm4_no_rw_after_crossing = function(
        command = { a0: Aircraft, a1: Aircraft ->
            one_crossed(a0, a1) implies {
                (to_the_right_of(a0, a1.position) && to_the_right_of(a1, a0.position))
                    ||
                    (to_the_left_of(a0, a1.position) && to_the_left_of(a1, a0.position))
            }
        }
    )

    val thm5_mutual_awareness = function(
        command = { a0: Aircraft, a1: Aircraft ->
            conv_not_headon(a0, a1)(airspace.properties.delta_o, airspace.properties.Theta_h) implies {
                (zero_crossed(a1, a0) && to_the_right_of(a0, a1.position)) implies {
                    right_of_way(a1, a0)(airspace.properties.delta_o,airspace.properties.delta_c, airspace.properties.Theta_h)
                }
            }
        }
    )

    val thm6_had_rw = function(
        command = { a0: Aircraft, a1: Aircraft ->
            (Q1(a0, a1.position) && Q1(a1, a0.position) && !parallel(a0, a1)) implies {
                isInQ1andWasInQ2(a0, a1)
                    ||
                    isInQ1andWasInQ2(a1, a0)
            }
        }
    )
}

