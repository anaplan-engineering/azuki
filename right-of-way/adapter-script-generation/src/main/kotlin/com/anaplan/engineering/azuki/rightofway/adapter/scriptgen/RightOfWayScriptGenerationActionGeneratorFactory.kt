package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayGenerate
import kotlin.reflect.KFunction

object RightOfWayScriptGenerationActionGeneratorFactory : RightOfWayActionGeneratorFactory {

    override fun generateAirspace(airspaceName: String) =
        dsl(RightOfWayGenerate::generateAirspace, airspaceName)

    override fun generateAirspaceWithAircraft(airspaceName: String, numberOfAircraft: UInt) =
        dsl(RightOfWayGenerate::generateAirspaceWithAircraft, airspaceName, numberOfAircraft)

    private fun dsl(fn: KFunction<*>, vararg values: Any?) = ScriptGenerationActionGenerator {
        RightOfWayScriptingHelper.scriptifyFunction(fn, *values)
    }
}
