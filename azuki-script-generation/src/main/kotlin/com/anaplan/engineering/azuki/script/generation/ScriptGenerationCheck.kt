package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

interface ScriptGenerationCheck<E : ScriptGenerationEnvironment> : Check {

    /**
     * Composes the check into the environment.
     * Returns a resolver which we can later use to get any composed checks for this check from the environment.
     * The default skips composition entirely.
     */
    fun composeInto(environment: E): ComposedCheckResolver<E> = ComposedCheckResolver { listOf(this) }

    /**
     * Gets the script for this check without trying to compose it.
     * May fail if the check is impossible to express as DSL without composition.
     */
    fun getCheckScript(environment: E): String
}

/**
 * Knows how to ask the environment for the final list of composed checks to substitute for a check submitted for
 * composition.
 */
fun interface ComposedCheckResolver<E: ScriptGenerationEnvironment> {

    fun resolveComposedChecks(environment: E): List<ScriptGenerationCheck<E>>
}
