package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

interface ScriptGenerationCheck<E : ScriptGenerationEnvironment> : Check {

    /**
     * Gets the script for this check without trying to compose it.
     * May fail if the check is impossible to express as DSL without composition.
     */
    fun getCheckScript(environment: E): String
}

interface ComposableScriptGenerationCheck<E : ScriptGenerationEnvironment> : ScriptGenerationCheck<E> {

    /**
     * Composes the check into the environment.
     * Returns a resolver which we can later use to get any composed checks for this check from the environment.
     * The default disables composition entirely.
     */
    fun composeInto(environment: E): ComposedCheckResolver<E>
}

/**
 * Attaches a composition step to a check that doesn't have one.
 */
inline fun <C : ScriptGenerationCheck<E>, E : ScriptGenerationEnvironment> C.composeAs(crossinline compose: C.(E) -> ComposedCheckResolver<E>): ComposableScriptGenerationCheck<E> =
    object : ComposableScriptGenerationCheck<E>, ScriptGenerationCheck<E> by this {

        override fun composeInto(environment: E) = this@composeAs.compose(environment)
    }

fun interface ComposedCheckResolver<E : ScriptGenerationEnvironment> {

    /**
     * Resolve the final composed checks to substitute for the original check submitted for composition.
     *
     * This should be called once all checks have been composed into the environment, and in the same relative position
     * in the check order as the original check.  Since each check that contributed to the composition will result in
     * a resolver call, the resolver should make sure that it doesn't duplicate any composed checks.
     *
     * Fails if composition isn't possible, at which point we should return the original check.
     */
    fun resolveComposedCheck(environment: E): Result<List<ScriptGenerationCheck<E>>>
}
