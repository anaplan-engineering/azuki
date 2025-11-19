package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.ActionGenerator

fun interface ScriptGenerationActionGenerator: ActionGenerator {

    fun getActionGeneratorScript(): String
}
