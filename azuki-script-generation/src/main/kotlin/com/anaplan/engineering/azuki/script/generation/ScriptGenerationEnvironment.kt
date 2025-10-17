package com.anaplan.engineering.azuki.script.generation

/**
 * Environment persisted through the script generation process.
 *
 * The environment type has itself as a parameter, as it returns various self-referential types.
 */
interface ScriptGenerationEnvironment {

    // More things may be added to this later.
}

/**
 * A script generation environment that can compose multiple checks into one check.
 *
 * The environment type has itself as a parameter, as it returns self-referential types.
 */
interface CheckComposingScriptGenerationEnvironment<C: ScriptGenerationCheck<*>>: ScriptGenerationEnvironment {

    /**
     * Checks to see if this environment has composed checks to substitute for the given check.
     * If so, return those; if not, return the empty list.
     */
    fun compositionOf(check: C): List<C>
}

/**
 * Dummy environment for users that don't need one.
 */
object NoScriptGenerationEnvironment : CheckComposingScriptGenerationEnvironment<ScriptGenerationCheck<NoScriptGenerationEnvironment>> {

    override fun compositionOf(check: ScriptGenerationCheck<NoScriptGenerationEnvironment>) = listOf(check)
}

fun interface ScriptGenerationEnvironmentFactory<E : ScriptGenerationEnvironment> {

    /**
     * Creates a fresh environment.
     */
    fun create(): E
}
