package com.anaplan.engineering.azuki.script.generation

/**
 * Environment persisted through the script generation process.
 */
interface ScriptGenerationEnvironment {

    /**
     * The list of checks produced from composing ComposableChecks through this environment.
     * Having been composed through the environment, they do not need access to it again.
     */
    val composedChecks: List<BasicScriptGenerationCheck>
}

/**
 * Dummy environment for users that don't need one.
 */
object NoScriptGenerationEnvironment : ScriptGenerationEnvironment {

    override val composedChecks = listOf<BasicScriptGenerationCheck>()
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
