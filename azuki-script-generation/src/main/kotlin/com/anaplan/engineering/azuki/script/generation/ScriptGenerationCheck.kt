package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

sealed interface ScriptGenerationCheck : Check

/**
 * A basic, non-composable script generation check that does not need an environment.
 */
interface BasicScriptGenerationCheck : ScriptGenerationCheck {

    fun getCheckScript(): String
}

/**
 * A basic, non-composable script generation check that uses an environment.
 */
interface EnvScriptGenerationCheck<in E : ScriptGenerationEnvironment> : ScriptGenerationCheck {

    fun getCheckScript(environment: E): String
}

/**
 * A check that can't be generated directly, but instead needs to be composed using an environment.
 */
interface ComposableScriptGenerationCheck<in E : ScriptGenerationEnvironment> : ScriptGenerationCheck {

    /**
     * Composes the check into the environment.
     * The final check resulting from this check (and possibly other checks) will be available in its composableChecks.
     */
    fun composeInto(environment: E)
}
