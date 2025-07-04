package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.ActionGenerator

interface ScriptGenerationActionGenerator: ActionGenerator {
    fun getActionGeneratorString(): String
}
