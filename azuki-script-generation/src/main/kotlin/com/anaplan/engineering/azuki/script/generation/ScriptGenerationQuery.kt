package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Query

interface ScriptGenerationQuery<T> : Query<T> {

    fun getQueryScript(): String
}

/**
 * A scriptable query over a collection, paired with a representative 'dummy' value from that collection.
 *
 * This is used when generating scripts for derived queries: the parent script is the driver of the derived query,
 * and the dummy value forms a placeholder for the result of the driver when generating the script of the query body.
 */
data class ScriptGenerationQueryWithDummy<C : Collection<T>, T>(
    val parent: ScriptGenerationQuery<C>, val dummy: T
) : ScriptGenerationQuery<C> by parent
