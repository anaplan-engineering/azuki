package com.anaplan.engineering.azuki.script.generation

/**
 * Environment persisted through the script generation process.
 */
interface ScriptGenerationEnvironment {

    // More things may be added to this later.
    // Most obligations on ScriptGenerationEnvironments are to be found in ScriptGenerationCheck, etc.
}

/**
 * Dummy environment for users that don't need one.
 */
object NoScriptGenerationEnvironment : ScriptGenerationEnvironment

fun interface ScriptGenerationEnvironmentFactory<E : ScriptGenerationEnvironment> {

    /**
     * Creates a fresh environment.
     */
    fun create(): E
}
