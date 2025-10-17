package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

interface ScriptGenerationCheck<E : ScriptGenerationEnvironment> : Check {

    /**
     * Composes the check into the environment.
     * Returns a resolver which we can later use to get any composed checks for this check from the environment.
     * The default skips composition entirely.
     */
    fun composeInto(environment: E): ComposedCheckResolver<E> = ComposedCheckResolver { this }

    /**
     * Gets the script for this check without trying to compose it.
     * May fail if the check is impossible to express as DSL without composition.
     */
    fun getCheckScript(environment: E): String
}

fun interface ComposedCheckResolver<E: ScriptGenerationEnvironment> {

    /**
     * Resolve the final composed check to substitute for the original check submitted for composition.
     * This should be called once all checks have been composed into the environment.
     * This may be null if the check was already accounted for in another resolution.
     */
    fun resolveComposedCheck(): ScriptGenerationCheck<E>?
}
