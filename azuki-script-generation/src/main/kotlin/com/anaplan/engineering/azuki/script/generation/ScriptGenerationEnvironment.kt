package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

/**
 * Environment persisted through the script generation process.
 */
interface ScriptGenerationEnvironment {

    /**
     * The list of checks produced from composing ComposableChecks through this environment.
     */
    val composedChecks: List<ScriptGenerationCheck>
}

/**
 * Dummy environment for users that don't need one.
 */
object NoScriptGenerationEnvironment : ScriptGenerationEnvironment {

    override val composedChecks = listOf<ScriptGenerationCheck>()
}

fun interface ScriptGenerationEnvironmentFactory<E : ScriptGenerationEnvironment> {

    /**
     * Creates a fresh environment.
     */
    fun create(): E

    // Creating throwaway environments is supported by default, but implementors can choose to set up specific types
    // of environment (or throw an exception!) if necessary.

    /**
     * Creates a throwaway environment for scriptifying a 'given' block in isolation.
     */
    fun createForGiven(): E = create()

    /**
     * Creates a throwaway environment for scriptifying a 'whenever' block in isolation.
     */
    fun createForWhenever(): E = create()

    /**
     * Creates a throwaway environment for scriptifying a 'then' block in isolation.
     */
    fun createForThen(): E = create()
}

/**
 * A check that can't be generated directly, but instead needs to be composed using an environment
 */
interface ComposableCheck<E : ScriptGenerationEnvironment> : Check {

    /**
     * Composes the check into the environment.
     * The final check resulting from this check (and possibly other checks) will be available in its composableChecks.
     */
    fun composeInto(environment: E)
}
