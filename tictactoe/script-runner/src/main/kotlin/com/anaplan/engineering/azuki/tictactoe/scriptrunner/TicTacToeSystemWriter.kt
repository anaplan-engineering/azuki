package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGeneration
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationQueryQueryFactory
import com.anaplan.engineering.azuki.verify.generation.ScriptGenerationSystemWriter
import java.io.File

class TicTacToeSystemWriter :
    ScriptGenerationSystemWriter<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>() {

    override val actionFactory = TicTacToeScriptGenerationActionFactory
    override val actionGeneratorFactory = TicTacToeScriptGenerationActionGeneratorFactory
    override val checkFactory = TicTacToeScriptGenerationCheckFactory
    override val queryFactory = TicTacToeScriptGenerationQueryQueryFactory

    override val outputDir: File get() = Command.outputDir
    override val scriptGeneration = TicTacToeScriptGeneration
    override val classGeneration = TicTacToeRunnableScenarioClassGenerator
}
