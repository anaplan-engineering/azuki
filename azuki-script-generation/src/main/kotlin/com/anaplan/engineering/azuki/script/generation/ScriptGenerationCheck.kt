package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

interface ScriptGenerationCheck<E : ScriptGenerationEnvironment> : Check {

    /**
     * Gets the script for this check.
     * If this check is composable, this returns the non-composed form, if any.
     */
    fun getCheckScript(environment: E): String
}

/**
 * A check that can be composed with other checks using an environment.
 */
interface ComposableScriptGenerationCheck<E : CheckComposingScriptGenerationEnvironment<*>> : ScriptGenerationCheck<E> {

    /**
     * Composes the check into the environment.
     * The final check resulting from this check (and possibly other checks) will be available in its composableChecks.
     */
    fun composeInto(environment: E)
}
