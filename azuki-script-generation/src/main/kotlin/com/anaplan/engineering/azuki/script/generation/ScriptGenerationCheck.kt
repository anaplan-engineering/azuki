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
     * Registers a composable check with the environment.
     * Returns an object that, after all registrations are done, can be used to get any composed results for this check.
     */
    fun registerComposable(environment: E): CheckComposer<E>
}

/**
 * Lifts a check to a composable check by applying the given composition registration step.
 */
inline fun <E : ScriptGenerationEnvironment> ScriptGenerationCheck<E>.asComposable(crossinline register: E.() -> CheckComposer<E>): ComposableScriptGenerationCheck<E> =
    object : ComposableScriptGenerationCheck<E>, ScriptGenerationCheck<E> by this {

        override fun registerComposable(environment: E) = environment.register()

        // Allow distinctiveness checks etc. to ignore this wrapper
        override fun equals(other: Any?) = this@asComposable == other
        override fun hashCode() = this@asComposable.hashCode()
        override fun toString() = this@asComposable.toString()
    }

