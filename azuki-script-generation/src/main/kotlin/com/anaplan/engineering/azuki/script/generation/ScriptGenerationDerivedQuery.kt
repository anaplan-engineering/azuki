package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.DerivedQuery

interface ScriptGenerationDerivedQuery<T> : DerivedQuery<T> {
    fun getDerivedQueryScript(): String
}
