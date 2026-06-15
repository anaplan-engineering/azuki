package com.anaplan.engineering.azuki.rightofway.scriptrunner

import com.anaplan.engineering.azuki.rightofway.adapter.api.*
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.TicTacToeRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.TicTacToeScriptGeneration
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.TicTacToeScriptGenerationActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGenerationActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGenerationCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.TicTacToeScriptGenerationQueryQueryFactory
import com.anaplan.engineering.azuki.verify.generation.ScriptGenerationSystemWriter
import java.io.File

class RightOfWaySystemWriter :
    ScriptGenerationSystemWriter<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>() {

    override val actionFactory = TicTacToeScriptGenerationActionFactory
    override val actionGeneratorFactory = RightOfWayScriptGenerationActionGeneratorFactory
    override val checkFactory = RightOfWayScriptGenerationCheckFactory
    override val queryFactory = TicTacToeScriptGenerationQueryQueryFactory

    override val outputDir: File get() = Command.outputDir
    override val scriptGeneration = TicTacToeScriptGeneration
    override val classGeneration = TicTacToeRunnableScenarioClassGenerator
}
