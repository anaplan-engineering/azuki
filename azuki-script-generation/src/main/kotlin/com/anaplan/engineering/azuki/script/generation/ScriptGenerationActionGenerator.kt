package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.ActionGenerator
import kotlin.reflect.KFunction

fun interface ScriptGenerationActionGenerator: ActionGenerator {

    fun getActionGeneratorScript(): String
}
