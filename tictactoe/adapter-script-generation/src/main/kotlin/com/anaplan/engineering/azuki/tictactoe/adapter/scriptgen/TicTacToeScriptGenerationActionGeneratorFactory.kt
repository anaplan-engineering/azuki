package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.InBoundsRandomPositionGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.InOrderTurnGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PositionGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.RandomTurnGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.RandomWalkPositionGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TurnGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.dsl.GeneratePositions
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGenerate
import com.anaplan.engineering.azuki.tictactoe.dsl.GenerateTurns

object TicTacToeScriptGenerationActionGeneratorFactory : TicTacToeActionGeneratorFactory {

    override fun generateMoves(
        gameName: String, moveCountRange: IntRange, turnStrategy: TurnGenerationStrategy, positionStrategy: PositionGenerationStrategy
    ) = ScriptGenerationActionGenerator {
        TicTacToeScriptingHelper.scriptifyFunction(
            TicTacToeGenerate::addMoves,
            moveCountRange,
            when (turnStrategy) {
                InOrderTurnGenerationStrategy -> GenerateTurns.InOrder
                RandomTurnGenerationStrategy -> GenerateTurns.Random
            },
            when (positionStrategy) {
                RandomWalkPositionGenerationStrategy -> GeneratePositions.RandomWalk
                InBoundsRandomPositionGenerationStrategy -> GeneratePositions.InBoundsRandom
            },
        )
    }
}
