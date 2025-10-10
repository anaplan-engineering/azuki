package com.anaplan.engineering.azuki.script.generation

/**
 * Environment persisted through the script generation process.
 */
interface ScriptGenerationEnvironment {

    // This may be extended in future
}

/**
 * Dummy environment for users that don't need one.
 */
object NoScriptGenerationEnvironment : ScriptGenerationEnvironment
