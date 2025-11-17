package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.actionGenerator

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.tokenAt

class SampleActionGeneratorFactory : TicTacToeActionGeneratorFactory {

    override fun generatePlayOrder(orderName: String) = SampleActionGenerator { env ->
        require(orderName !in env.playOrders) { "play order $orderName already generated or declared" }

        // We need to shuffle these ahead of time; otherwise, the order will keep changing between system iterations
        val players = listOf("X", "O").shuffled()

        listOf { af -> af.playOrder.create(orderName, players) }
    }

    override fun generateNewGame(gameName: String) = SampleActionGenerator { env ->
        require(gameName !in env.gameManager.activeGames) { "game $gameName already generated or declared" }

        // We need to shuffle these ahead of time; otherwise, the order will keep changing between system iterations
        val orderName = env.playOrders.keys.random()

        listOf { af -> af.game.start(gameName, orderName) }
    }

    override fun generateMoves(gameName: String, numMoves: Int) = SampleActionGenerator { env ->
        env.withGame(gameName) {
            require(0 <= numMoves) { "number of moves must be non-negative" }

            // Make sure we skip however many turns have already been taken when deciding which turn goes next
            val movesSoFar = playOrder.sumOf { playerMoveCount(it) }

            val symbols = playOrder.map { it.token.symbol }
            val turns = sequence { while (true) yieldAll(symbols) }.drop(movesSoFar)

            val positions = (1..width).asSequence().flatMap { x ->
                (1..height).map { y -> Position(row = y, col = x) }.filter { tokenAt(it) == null }
            }.shuffled().take(numMoves)

            turns.zip(positions) { playerName, position ->
                { af: TicTacToeActionFactory -> af.game.move(gameName, playerName, position) }
            }.toList()
        }
    }
}

fun interface SampleActionGenerator : ActionGenerator {

    fun generate(env: ExecutionEnvironment): List<(TicTacToeActionFactory) -> Action>
}
