package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGenerate
import kotlin.reflect.KFunction

object TicTacToeScriptGenerationActionGeneratorFactory : TicTacToeActionGeneratorFactory {

    override fun generatePlayOrder(orderName: String) = dsl(TicTacToeGenerate::createPlayOrder, orderName)

    override fun generateGame(gameName: String) = dsl(TicTacToeGenerate::createNewGameFromExistingPlayOrder, gameName)

    override fun generateMoves(gameName: String, numMoves: Int) =
        dsl(TicTacToeGenerate::addMoves, gameName, numMoves)

    private fun dsl(fn: KFunction<*>, vararg values: Any?) = ScriptGenerationActionGenerator {
        TicTacToeScriptingHelper.scriptifyFunction(fn, *values)
    }
}
