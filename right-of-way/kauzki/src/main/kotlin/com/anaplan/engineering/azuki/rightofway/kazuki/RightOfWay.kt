package com.anaplan.engineering.azuki.rightofway.kazuki

//import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay_Module.mk_Game
//import com.anaplan.engineering.azuki.rightofway.kazuki.RightOfWay_Module.mk_Position
import com.anaplan.engineering.kazuki.core.*

@Module
object RightOfWay {
    const val test = 1UL

    @PrimitiveInvariant(name = "PReal", base = Double::class)
    fun pReal(r: Double) = r >= 0.0

    @PrimitiveInvariant(name = "NZReal", base = Double::class)
    fun nzReal(r: Double) = r != 0.0

    @PrimitiveInvariant(name = "NReal", base = Double::class)
    fun nReal(r: Double) = r < 0.0

//    @PrimitiveInvariant(name = "PNZReal", base = PReal::class)
    @PrimitiveInvariant(name = "PNZReal", base = Double::class)
    fun pnzReal(r: Double) = pReal(r) && nzReal(r)

    @PrimitiveInvariant(name = "NNZReal", base = Double::class)
    fun nnzReal(r: Double) = nReal(r) && nzReal(r)

    @PrimitiveInvariant(name = "Angle", base = Double::class)
    fun planeAngle(r: Double) = pReal(r) && r < 360.0

    // plane position
    interface Position {
        val x: PReal
        val y: PReal
    }

    // plane velocity
    interface Velocity {
        val x: PNZReal
        val y: PNZReal
        val dummy: bool // to disambiguate on the Velocity_Rec x Position_Rec :-(
    }

    interface Aircraft {
        val position: Position
        val velocity: Velocity
    }
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
