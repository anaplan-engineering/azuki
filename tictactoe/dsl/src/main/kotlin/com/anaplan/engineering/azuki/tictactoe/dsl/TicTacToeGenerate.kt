package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory

class TicTacToeGenerate(private val actionGeneratorFactory: TicTacToeActionGeneratorFactory) :
    Generate<TicTacToeActionGeneratorFactory> {

    fun addMoves(gameName: String, moveCountRange: IntRange = (0..9)) =
        addGenerator { generateMoves(gameName, moveCountRange) }

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList

    private fun addGenerator(via: TicTacToeActionGeneratorFactory.() -> ActionGenerator) {
        generatorList.add(actionGeneratorFactory.via())
    }
}
