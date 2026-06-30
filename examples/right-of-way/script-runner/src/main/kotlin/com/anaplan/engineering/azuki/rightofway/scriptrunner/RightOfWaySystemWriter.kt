package com.anaplan.engineering.azuki.rightofway.scriptrunner

import com.anaplan.engineering.azuki.rightofway.adapter.api.*
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGeneration
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGenerationActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGenerationActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGenerationCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGenerationQueryQueryFactory
import com.anaplan.engineering.azuki.verify.generation.ScriptGenerationSystemWriter
import java.io.File

class RightOfWaySystemWriter :
    ScriptGenerationSystemWriter<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory>() {

    override val actionFactory = RightOfWayScriptGenerationActionFactory
    override val actionGeneratorFactory = RightOfWayScriptGenerationActionGeneratorFactory
    override val checkFactory = RightOfWayScriptGenerationCheckFactory
    override val queryFactory = RightOfWayScriptGenerationQueryQueryFactory

    override val outputDir: File get() = Command.outputDir
    override val scriptGeneration = RightOfWayScriptGeneration
    override val classGeneration = RightOfWayRunnableScenarioClassGenerator
}
