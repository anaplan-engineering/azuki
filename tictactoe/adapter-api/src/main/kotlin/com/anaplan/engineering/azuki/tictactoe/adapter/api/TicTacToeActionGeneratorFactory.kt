package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import kotlin.random.Random

interface TicTacToeActionGeneratorFactory : ActionGeneratorFactory {

    /**
     * Generates a sequence of moves.
     */
    fun generateMoves(
        gameName: String, moveCountRange: IntRange, turnStrategy: TurnGenerationStrategy, positionStrategy: PositionGenerationStrategy
    ): ActionGenerator
}

sealed interface TurnGenerationStrategy {

    fun generateTurns(playOrder: List<String>, random: Random): Sequence<String>
}

object InOrderTurnGenerationStrategy : TurnGenerationStrategy {

    override fun generateTurns(playOrder: List<String>, random: Random) = sequence { while (true) yieldAll(playOrder) }
}

object RandomTurnGenerationStrategy : TurnGenerationStrategy {

    override fun generateTurns(playOrder: List<String>, random: Random) = generateSequence {
        playOrder.random(random)
    }
}

sealed interface PositionGenerationStrategy {

    fun generatePositions(width: Int, height: Int, random: Random): Sequence<Position>
}

/**
 * Randomly visits each valid position once.
 */
object RandomWalkPositionGenerationStrategy : PositionGenerationStrategy {

    override fun generatePositions(width: Int, height: Int, random: Random) = (1..width).asSequence().flatMap { x ->
        (1..height).asSequence().map { y ->
            Position(x, y)
        }
    }.shuffled(random)
}

/**
 * Chooses random positions that are in-bounds but may be duplicates.
 */
object InBoundsRandomPositionGenerationStrategy : PositionGenerationStrategy {

    override fun generatePositions(width: Int, height: Int, random: Random) = generateSequence {
        val x = (1..width).random(random)
        val y = (1..height).random(random)
        Position(x, y)
    }
}
