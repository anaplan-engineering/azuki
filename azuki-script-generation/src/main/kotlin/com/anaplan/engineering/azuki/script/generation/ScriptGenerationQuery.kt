package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Query

interface ScriptGenerationQuery<T> : Query<T> {
    fun getQueryScript(): String
}

