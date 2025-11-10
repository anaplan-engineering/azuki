package com.anaplan.engineering.azuki.script.generation

data class ScriptGenerationQueryWithDummy<C : Collection<T>, T>(
    val parent: ScriptGenerationQuery<C>, val dummy: T
) : ScriptGenerationQuery<C> by parent
