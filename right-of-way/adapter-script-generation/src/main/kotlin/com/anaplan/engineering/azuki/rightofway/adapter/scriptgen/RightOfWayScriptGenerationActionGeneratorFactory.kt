package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayGenerate
import kotlin.reflect.KFunction

object RightOfWayScriptGenerationActionGeneratorFactory : RightOfWayActionGeneratorFactory {

    override fun generateAirspace(airspaceName: String, delta_o: Double, delta_c: Double , theta_h: Double, opened: Boolean) =
        dsl(RightOfWayGenerate::generateAirspace, airspaceName, delta_o, delta_c, theta_h, opened)

    override fun generateAirspaceWithAircraft(airspaceName: String, numberOfAircraft: UInt) =
        dsl(RightOfWayGenerate::generateAirspaceWithAircraft, airspaceName, numberOfAircraft)

    private fun dsl(fn: KFunction<*>, vararg values: Any?) = ScriptGenerationActionGenerator {
        RightOfWayScriptingHelper.scriptifyFunction(fn, *values)
    }
}
