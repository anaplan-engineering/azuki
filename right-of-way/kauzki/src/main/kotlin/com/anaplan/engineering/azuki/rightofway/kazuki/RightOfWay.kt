package com.anaplan.engineering.azuki.rightofway.kazuki

import com.anaplan.engineering.azuki.rightofway.kazuki.Position_Module.as_Position
import com.anaplan.engineering.azuki.rightofway.kazuki.RVector_Module.mk_RVector
import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay.orientation
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
object RightOfWay {

//    interface RVector {
//        val x: Double
//        val y: Double
//
//            @Invariant
//            fun isReal() = isReal(x) && isReal(y)
//        }
//    // plane position
//    interface Position {
//        val x: PReal
//        val y: PReal
//        val other: integer
//    }
    //    interface Velocity {
//        val x: PNZReal
//        val y: PNZReal
//        val dummy: bool // to disambiguate on the Velocity_Rec x Position_Rec :-(
//    }

//    interface Velocity : Position {
//        val dummy: Boolean // must have fields error
//        @Invariant
//        fun isValid() = x > 0.0 && y > 0.0
//    }

    interface Aircraft {
        val position: Position
        val velocity: Velocity
    }

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
        command = { a1: Aircraft, a2: Aircraft ->
            if (a1.velocity == a2.velocity)
                0.0 // if 0, type gets captured as Number!!!!
            else {
                val pDiff = as_Position(subtractVectors(a1.position, a2.position))
                val vDiff = as_Velocity(subtractVectors(a1.velocity, a2.velocity))
                // Because Velocity are different, then their difference is not zero
                val vDiffProd = dot_product(vDiff, vDiff)
                (- ( dot_product(pDiff, vDiff) / vDiffProd)) as Double // why isn't result double? as Double
            }
        },
        //pre fails for example!
        post = { _, _, r -> isNZReal(r) }
    )

    // Horizontal Miss Distance is the distance at the Closest Point of Approach
    // when their position and velocity vectors are projected in time.
    val horizontalMissDistance = function(
        command = { a1: Aircraft, a2: Aircraft ->
            magnitude(
                sumVectors(
                    subtractVectors(a1.position, a2.position),
                    scalar_product(
                        tCPA(a1, a2),
                        subtractVectors(a1.velocity, a2.velocity)
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
        command = { a1: Aircraft, a2: Aircraft ->
            dot_product(a1.velocity, a2.velocity) < 0.0
        }
    )

    val right_to_left = function(
        command = { a1: Aircraft, a2: Aircraft ->
            dot_product(a1.velocity, a2.velocity) > 0.0
        }
    )

    // Check trajectories will cross in future
    val going_to_cross = function(
        command = { a1: Aircraft, a2: Aircraft ->
            to_the_left_of(a1, a2.position) && left_to_right(a1, a2)
                ||
                to_the_right_of(a1, a2.position) && right_to_left(a1, a2)
        }
    )

    // Check trajectories have already crossed
    val crossed = function(
        command = { a1: Aircraft, a2: Aircraft ->
            to_the_left_of(a1, a2.position) && right_to_left(a1, a2)
                ||
                to_the_right_of(a1, a2.position) && left_to_right(a1, a2)
        }
    )

    // Check trajectories are going to cross but have not yet crossed
    val zero_crossed = function(
        command = { a1: Aircraft, a2: Aircraft ->
            going_to_cross(a1, a2) && going_to_cross(a2, a1)
        }
    )

    // One aircraft (a1) has crossed trajectory of other (a2) but not other way round
    val one_crossed = function(
        command = { a1: Aircraft, a2: Aircraft ->
            going_to_cross(a1, a2) && crossed(a1, a2)
                ||
                going_to_cross(a2, a1) && crossed(a2, a1)
        }
    )

    // both aircraft crossed each other's trajectories
    val both_crossed = function(
        command = { a1: Aircraft, a2: Aircraft ->
            crossed(a1, a2) && crossed(a2, a1)
        }
    )

    val orientation = function(
        command = { a1: Aircraft, a2: Aircraft ->
            dot_product(a1.velocity, rotate90(a2.velocity)) //as Angle
        },
        //post = { _, _, r -> isAngle(r) }
    )
    val parallel = function(
        command = { a1: Aircraft, a2: Aircraft ->
            orientation(a1, a2) == 0.0
        }
    )

    val same_orientation = function(
        command = { a1: Aircraft, a2: Aircraft ->
            orientation(a1, a2) > 0.0
        }
    )

    val opposite_orientation = function(
        command = { a1: Aircraft, a2: Aircraft ->
            orientation(a1, a2) < 0.0
        }
    )

    val isInQ1andWasInQ2 = function(
        command = { a1: Aircraft, a2: Aircraft ->
            Q1(a1, a2.position) && Q1(a2, a1.position)
                ||
                (opposite_orientation(a1, a2) and left_to_right(a1, a2));
        }
    )

    // Quadrant convergence
    val QC = function(
        command = { a1: Aircraft, a2: Aircraft ->
            Q1(a1, a2.position) && Q1(a2, a1.position)
                ||
                Q2(a1, a2.position) && Q2(a2, a1.position)
                ||
                Q3(a1, a2.position) && Q3(a2, a1.position)
                ||
                Q4(a1, a2.position) && Q4(a2, a1.position)
        }
    )

    val converging = function(
        command = { a1: Aircraft, a2: Aircraft ->
            val inner = function(
                command = { delta_c: PReal ->
                    QC(a1, a2) &&
                        horizontalMissDistance(a1, a2) < delta_c
                }
            )
            inner
        }
    )

    val conv_not_headon = function(
        command = { a1: Aircraft, a2: Aircraft ->
            val inner = function(
                command = { delta_c: PReal, delta_h: Angle ->
                    val track_delta = abs(track(a1) - track(a2))
                    converging(a1, a2)(delta_c) &&
                        (180 + delta_h < track_delta)
                        ||
                        (track_delta < 180 - delta_h)
                }
            )
            inner
        }
    )

    val headon = function(
        command = { a1: Aircraft, a2: Aircraft ->
            val inner = function(
                command = { delta_c: PReal, delta_h: Angle ->
                    val track_delta = abs(track(a1) - track(a2))
                    converging(a1, a2)(delta_c) &&
                        (180 + delta_h <= track_delta)
                        ||
                        (track_delta < 180 + delta_h)
                }
            )
            inner
        }
    )

    val overtaking = function(
        command = { a1: Aircraft, a2: Aircraft ->
            val inner = function(
                command = { delta_o: PReal ->
                    Q1(a1, a2.position) || Q2(a1, a2.position)
                        &&
                    Q3(a2, a1.position) || Q4(a2, a1.position)
                        &&
                        horizontalMissDistance(a1, a2) < delta_o
                }
            )
            inner
        }
    )

    val right_of_way = function(
        command = { a1: Aircraft, a2: Aircraft ->
            val inner = function(
                command = { delta_o: PReal, delta_c: PReal, delta_h: PReal ->
                    overtaking(a1, a2)(delta_o)
                        ||
                       ( conv_not_headon(a1, a2)(delta_c, delta_h)
                        &&
                        to_the_right_of(a1, a2.position)
                        &&
                        zero_crossed(a1, a2))
                }
            )
            inner
        }
    )

    /*
     */
}

//
//@Module
//object XO {
//
//    const val Size = 3uL
//
//    val MaxMoves = Size * Size
//
//    enum class Player {
//        Nought,
//        Cross
//    }
//
//    interface Position {
//        val row: Coord
//        val col: Coord
//    }
//
//    @PrimitiveInvariant(name = "Coord", base = nat1::class)
//    fun coordInvariant(c: nat) = c in 1uL..Size
//
//    // A legal game play sequence
//    interface Moves : Sequence1<Position> {
//        @Invariant
//        fun noDuplicatePositions() = len == elems.card
//
//        @Invariant
//        fun hasMinMovesToWin() = len > Players.card * (Size - 1uL)
//
//        @Invariant
//        fun doesntHaveTooManyMoves() = len <= MaxMoves
//    }
//
//    val Players = asSet<Player>()
//
//    interface PlayOrder : Sequence1<Player> {
//
//        @Invariant
//        fun noDuplicatePlayers() = len == elems.card
//
//        @Invariant
//        fun correctNumberOfPlayers() = elems == Players
//    }
//
//    val S: Set<nat1> = as_Set(1uL..Size)
//
//    val winningLines: Set<Set<Position>> = dunion(
//        mk_Set(
//            set(S) { r: nat1 -> set(S) { c: nat1 -> mk_Position(r, c) } },
//            set(S) { c: nat1 -> set(S) { r: nat1 -> mk_Position(r, c) } },
//            mk_Set(
//                as_Set(set(S) { x: nat1 -> mk_Position(x, x) }),
//                as_Set(set(S) { x: nat1 -> mk_Position(x, Size - x + 1u) })
//            )
//        ),
//    )
//
//    interface Game {
//        val board: Mapping<Position, Player>
//        val order: PlayOrder
//
//        @Invariant
//        fun cantHaveMoreThanMaxMoves() = moveCountLeft(this) >= 0uL
//
//        @Invariant
//        fun noPlayerMoreThanOneMoveAhead() =
//            forall(order.inds - order.len) { i ->
//                val current = order[i]
//                val next = order[i + 1uL]
//                movesForPlayer(this, current).card - movesForPlayer(this, next).card in mk_Set(0uL, 1uL)
//            }
//    }
//
//    val hasWon = function(
//        command = { g: Game, p: Player ->
//            val moves = movesForPlayer(g, p)
//            exists(winningLines) { line -> line subset moves }
//        }
//    )
//
//    val hasLost = function(
//        command = { g: Game, p: Player ->
//            !hasWon(g, p)
//        }
//    )
//
//    val whoWon = function(
//        command = { g: Game -> iota(Players) { p -> hasWon(g, p) } },
//        pre = { g -> isWon(g) }
//    )
//
//    val isWon = function(
//        command = { g: Game -> exists1(Players) { p -> hasWon(g, p) } },
//    )
//
//    val isDraw = function(
//        command = { g: Game -> (!(isWon(g)) and (moveCountLeft(g) == 0uL)) },
//    )
//
//    val isUnfinished = function(
//        command = { g: Game -> (!(isWon(g)) and !(isDraw(g))) },
//    )
//
//    val movesSoFar = function(
//        command = { g: Game -> g.board.dom }
//    )
//
//    val moveCountSoFar = function(
//        command = { g: Game -> movesSoFar(g).card }
//    )
//
//    val moveCountLeft = function(
//        command = { g: Game -> MaxMoves - moveCountSoFar(g) }
//    )
//
//    val movesForPlayer = function(
//        command = { g: Game, p: Player -> (g.board rrt mk_Set(p)).dom }
//    )
//
//    val move = function(
//        command = { g: Game, p: Player, pos: Position ->
//            mk_Game(g.board + mk_Mapping(mk_(pos, p)), g.order)
//        },
//        pre = { g, p, pos -> canMove(g, p, pos) },
//        post = { g, _, _, result ->
//            moveCountSoFar(result) == moveCountSoFar(g) + 1uL
//        }
//    )
//
//    val canMove = function(
//        command = { g: Game, p: Player, pos: Position ->
//            hasTurn(g, p) &&
//                pos !in movesSoFar(g) &&
//                moveCountLeft(g) > 0uL
//        }
//    )
//
//    val hasTurn = function(
//        command = { g: Game, p: Player ->
//            val order = g.order
//            val numPlayers = order.len
//            val numMoves = movesSoFar(g).card
//            order[(numMoves % numPlayers) + 1uL] == p
//        }
//    )
//}
