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
 * Attaches a composition step to a check that doesn't have one.
 */
inline fun <C : ScriptGenerationCheck<E>, E : ScriptGenerationEnvironment> C.composeAs(crossinline compose: C.(E) -> ComposedCheckResolver<E>) =
    object : ScriptGenerationCheck<E> {

        override val behavior get() = this@composeAs.behavior
        override fun getCheckScript(environment: E) = this@composeAs.getCheckScript(environment)
        override fun composeInto(environment: E) = this@composeAs.compose(environment)
    }

fun interface ComposedCheckResolver<E : ScriptGenerationEnvironment> {

    /**
     * Resolve the final composed checks to substitute for the original check submitted for composition.
     * This should be called once all checks have been composed into the environment.
     */
    fun resolveComposedCheck(environment: E): List<ScriptGenerationCheck<E>>
}
