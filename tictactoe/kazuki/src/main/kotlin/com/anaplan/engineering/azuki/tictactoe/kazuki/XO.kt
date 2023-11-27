package com.anaplan.engineering.azuki.tictactoe.kazuki

import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module.mk_Game
import com.anaplan.engineering.azuki.tictactoe.kazuki.XO_Module.mk_Position
import com.anaplan.engineering.kazuki.core.*

// TODO -- invariants should include message
// TODO -- is_<Moves>() and as_<Moves>


@Module
object XO {

    const val Size = 3

    const val MaxMoves = Size * Size

    enum class Player {
        Nought,
        Cross
    }

    interface Position {
        val row: Coord
        val col: Coord
    }

    @PrimitiveInvariant(name = "Coord", base = Int::class)
    fun coordInvariant(c: Int) = c in 1..Size

    // A legal game play sequence
    interface Moves : Sequence1<Position> {
        @Invariant
        fun noDuplicatePositions() = len == elems.card

        @Invariant
        fun hasMinMovesToWin() = len > Players.card * (Size - 1)

        @Invariant
        fun doesntHaveTooManyMoves() = len <= MaxMoves
    }

    val Players = asSet<Player>()

    interface PlayOrder : Sequence1<Player> {

        @Invariant
        fun noDuplicatePlayers() = len == elems.card

        @Invariant
        fun correctNumberOfPlayers() = elems == Players
    }

    val S: Set<nat1> = mk_Set(1..Size)

    val winningLines: Set<Set<Position>> = dunion(
        set(selector = { r: nat1 -> set(selector = { c: nat1 -> mk_Position(r, c) }, S) }, S),
        set(selector = { c: nat1 -> set(selector = { r: nat1 -> mk_Position(r, c) }, S) }, S),
        mk_Set(
            mk_Set(set(selector = { x: nat1 -> mk_Position(x, x) }, S)),
            mk_Set(set(selector = { x: nat1 -> mk_Position(x, Size - x + 1) }, S))
        ),
    )

    interface Game {
        val board: Map<Position, Player>
        val order: PlayOrder

        @Invariant
        fun cantHaveMoreThanMaxMoves() = moveCountLeft(this) >= 0

        @Invariant
        fun noPlayerMoreThanOneMoveAhead() =
            forall(order.inds - order.len) { i ->
                val current = order[i]
                val next = order[i + 1]
                movesForPlayer(this, current).card - movesForPlayer(this, next).card in mk_Set(0, 1)
            }
    }

    val hasWon = function(
        command = { g: Game, p: Player ->
            val moves = movesForPlayer(g, p)
            exists(winningLines) { line -> line subset moves }
        }
    )

    val hasLost = function(
        command = { g: Game, p: Player ->
            !hasWon(g, p)
        }
    )

    val whoWon = function(
        command = { g: Game -> iota(Players) { p -> hasWon(g, p) } },
        pre = { g -> isWon(g) }
    )

    val isWon = function(
        command = { g: Game -> exists1(Players) { p -> hasWon(g, p) } },
    )

    val isDraw = function(
        command = { g: Game -> (!(isWon(g)) and (moveCountLeft(g) == 0)) },
    )

    val isUnfinished = function(
        command = { g: Game -> (!(isWon(g)) and !(isDraw(g))) },
    )

    val movesSoFar = function(
        command = { g: Game -> g.board.dom() }
    )

    val moveCountSoFar = function(
        command = { g: Game -> movesSoFar(g).card }
    )

    val moveCountLeft = function(
        command = { g: Game -> MaxMoves - moveCountSoFar(g) }
    )

    val movesForPlayer = function(
        command = { g: Game, p: Player -> (g.board rrt mk_Set(p)).dom() }
    )

    val move = function(
        command = { g: Game, p: Player, pos: Position ->
            mk_Game(g.board munion mk_Map(pos to p), g.order)
        },
        pre = { g, p, pos ->
            hasTurn(g, p) &&
                pos !in movesSoFar(g) &&
                moveCountLeft(g) > 0
        },
        post = { g, _, _, result ->
            moveCountSoFar(result) == moveCountSoFar(g) + 1
        }
    )

    val hasTurn = function(
        command = { g: Game, p: Player ->
            val order = g.order
            val numPlayers = order.len
            val numMoves = movesSoFar(g).card
            order[(numMoves % numPlayers) + 1] == p
        }
    )

}

