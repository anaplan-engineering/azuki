package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.actionGenerator

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PositionGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TurnGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import kotlin.random.Random

class SampleActionGeneratorFactory : TicTacToeActionGeneratorFactory {

    val random = Random.Default

    override fun generateMoves(
        gameName: String,
        moveCountRange: IntRange,
        turnStrategy: TurnGenerationStrategy,
        positionStrategy: PositionGenerationStrategy,
    ) = SampleActionGenerator { env ->
        env.withGame(gameName) {
            require(0 <= moveCountRange.first) { "minimum must be non-negative" }
            require(!moveCountRange.isEmpty()) { "move count range must be non-empty" }

            val numMoves = moveCountRange.random(random)

            val symbols = playOrder.map { it.token.symbol }
            val turnOrder = turnStrategy.generateTurns(symbols, random)
            val turns = turnOrder.take(numMoves)
            val positions = positionStrategy.generatePositions(width, height, random)

            turns.zip(positions) { playerName, position ->
                { it: TicTacToeActionFactory -> it.game.move(gameName, playerName, position) }
            }.toList()
        }
    }
}

fun interface SampleActionGenerator : ActionGenerator {

    fun generate(env: ExecutionEnvironment): List<(TicTacToeActionFactory) -> Action>
}
