package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Action

interface ScriptGenerationAction : Action {
    fun getActionScript(): String
}


