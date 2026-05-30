package com.anaplan.engineering.azuki.rightofway.kazuki

//import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay_Module.mk_Game
//import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay_Module.mk_RVector
import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay.dot_product
import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay.to_the_right_of
import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay_Module.mk_RVector
import com.anaplan.engineering.kazuki.core.*

@Module
object RightOfWay {
    const val DEFAULT_SQRT_ERROR: PNZReal = 0.000001;

    @PrimitiveInvariant(name = "Real", base = Double::class)
    fun isReal(r: Double) = !r.isNaN() && !r.isInfinite()

    @PrimitiveInvariant(name = "PReal", base = Double::class)
    fun pReal(r: Double) = isReal(r) && r >= 0.0

    @PrimitiveInvariant(name = "NZReal", base = Double::class)
    fun nzReal(r: Double) = isReal(r) && r != 0.0

    @PrimitiveInvariant(name = "NReal", base = Double::class)
    fun nReal(r: Double) = isReal(r) && r < 0.0

//    @PrimitiveInvariant(name = "PNZReal", base = PReal::class)
    @PrimitiveInvariant(name = "PNZReal", base = Double::class)
    fun pnzReal(r: Double) = pReal(r) && nzReal(r)

    @PrimitiveInvariant(name = "NNZReal", base = Double::class)
    fun nnzReal(r: Double) = nReal(r) && nzReal(r)

    @PrimitiveInvariant(name = "Angle", base = Double::class)
    fun planeAngle(r: Double) = pReal(r) && r < 360.0

    interface RVector {
        val x: Double
        val y: Double

        @Invariant
        fun isReal() = isReal(x) && isReal(y)
    }

    // plane position
    interface Position {
        val x: PReal
        val y: PReal
        val other: integer
    }

    //typealias Position = RVector + PReal(x) + PReal(y)

    // plane velocity
    interface Velocity {
        val x: PNZReal
        val y: PNZReal
        val dummy: bool // to disambiguate on the Velocity_Rec x Position_Rec :-(
    }

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
        post = { u, v, _ ->
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

    //TODO when to use a function like this or an @FunctionProvider??
    val Q1 = function(
        command = { a: Aircraft, p: Position ->
            val psub = subtractVectors(p, a.position)
            dot_product(psub, rotate90(a.velocity)) > 0.0
                &&
                dot_product(psub, a.velocity) >= 0.0
        },
        post = { a, p, r ->
            r implies to_the_right_of(a, p)
        }
    )

//    val Q2 = function(
//        command = { }
//    )



    val to_the_right_of = function(
        command = { a: Aircraft, p: Position ->
            dot_product(
                subtractVectors(p, a.position),
                rotate90(a.velocity)) > 0.0
        }
    )

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
