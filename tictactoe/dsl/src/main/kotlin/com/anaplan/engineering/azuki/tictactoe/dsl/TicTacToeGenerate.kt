package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.InBoundsRandomPositionGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.InOrderTurnGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.RandomTurnGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.RandomWalkPositionGenerationStrategy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory

class TicTacToeGenerate(private val actionGeneratorFactory: TicTacToeActionGeneratorFactory) :
    Generate<TicTacToeActionGeneratorFactory> {

    fun addMoves(
        gameName: String,
        moveCountRange: IntRange = (0..9),
        turns: GenerateTurns = GenerateTurns.InOrder,
        positions: GeneratePositions = GeneratePositions.RandomWalk,
    ) = addGenerator {
        val turnStrategy = when (turns) {
            GenerateTurns.InOrder -> InOrderTurnGenerationStrategy
            GenerateTurns.Random -> RandomTurnGenerationStrategy
        }
        val positionStrategy = when (positions) {
            GeneratePositions.RandomWalk -> RandomWalkPositionGenerationStrategy
            GeneratePositions.InBoundsRandom -> InBoundsRandomPositionGenerationStrategy
        }
        generateMoves(gameName, moveCountRange, turnStrategy, positionStrategy)
    }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: TicTacToeActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}

enum class GenerateTurns {

    InOrder, Random,
}

enum class GeneratePositions {

    RandomWalk, InBoundsRandom,
}
