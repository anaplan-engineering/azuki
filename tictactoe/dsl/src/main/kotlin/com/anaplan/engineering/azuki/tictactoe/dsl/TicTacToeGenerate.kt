package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Generate
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory

class TicTacToeGenerate(private val actionGeneratorFactory: TicTacToeActionGeneratorFactory) :
    Generate<TicTacToeActionGeneratorFactory> {

    private val generatorList = mutableListOf<ActionGenerator>()

    override fun generators() = generatorList
}
