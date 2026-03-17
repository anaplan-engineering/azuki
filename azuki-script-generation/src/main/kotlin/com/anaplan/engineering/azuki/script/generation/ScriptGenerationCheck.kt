package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior

interface ScriptGenerationCheck<E : ScriptGenerationEnvironment> : Check {

    /**
     * Gets the script for this check without trying to compose it.
     * May throw if the check is impossible to express as DSL without composition.
     */
    @Throws(IllegalStateException::class)
    fun getCheckScript(environment: E): String

    /**
     * As `getCheckScript`, but returns a renderable script element instead of a string.
     *
     * By default, this returns an element that wraps the contents of `getCheckScript`.
     * Override this for more control over the script generation of a check.
     * (If doing so, consider making `getCheckScript` call the `render` method of the script element for consistency.)
     */
    @Throws(IllegalStateException::class)
    fun getCheckScriptElement(environment: E): ScriptElement = ScriptStringFragment(getCheckScript(environment))
}

interface ComposableScriptGenerationCheck<E : ScriptGenerationEnvironment> : ScriptGenerationCheck<E> {

    /**
     * Registers this composable check with the environment.
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

/**
 * Produces a composable-only check.
 */
inline fun <E : ScriptGenerationEnvironment> onlyComposable(crossinline register: E.() -> CheckComposer<E>): ComposableScriptGenerationCheck<E> =
    object : ComposableScriptGenerationCheck<E> {

        override val behavior = unsupportedBehavior
        override fun getCheckScript(environment: E) = error("This check cannot be scriptified directly and must be composed")
        override fun getCheckScriptElement(environment: E) = error("This check cannot be scriptified directly and must be composed")
        override fun registerComposable(environment: E) = environment.register()
    }
