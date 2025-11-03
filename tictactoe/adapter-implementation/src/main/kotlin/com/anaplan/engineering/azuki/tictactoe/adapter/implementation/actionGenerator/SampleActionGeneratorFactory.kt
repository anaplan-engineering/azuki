package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.actionGenerator

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment

class SampleActionGeneratorFactory : TicTacToeActionGeneratorFactory {
}

interface SampleActionGenerator : ActionGenerator {

    fun generate(env: ExecutionEnvironment): List<(TicTacToeActionFactory) -> Action>
}
