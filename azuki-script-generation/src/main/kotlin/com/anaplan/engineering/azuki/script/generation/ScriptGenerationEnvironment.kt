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
interface CheckComposingScriptGenerationEnvironment<out C: ScriptGenerationCheck<*>>: ScriptGenerationEnvironment {

    /**
     * The list of checks produced so far from composing checks through this environment.
     */
    val composedChecks: List<C>
}

/**
 * Dummy environment for users that don't need one.
 */
object NoScriptGenerationEnvironment : CheckComposingScriptGenerationEnvironment<ScriptGenerationCheck<NoScriptGenerationEnvironment>> {

    override val composedChecks = emptyList<ScriptGenerationCheck<NoScriptGenerationEnvironment>>()
}

fun interface ScriptGenerationEnvironmentFactory<E : ScriptGenerationEnvironment> {

    /**
     * Creates a fresh environment.
     */
    fun create(): E
}
