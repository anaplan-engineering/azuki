package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGenerate

object TicTacToeScriptGenerationActionGeneratorFactory : TicTacToeActionGeneratorFactory {

    override fun generateMoves(gameName: String, moveCountRange: IntRange) = ScriptGenerationActionGenerator {
        TicTacToeScriptingHelper.scriptifyFunction(TicTacToeGenerate::addMoves, moveCountRange)
    }
}
