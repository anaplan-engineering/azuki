package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

/**
 * A basic, non-composable script generation check.
 * Checks that need to compose with other checks, or reference the environment, should be stated as ComposableChecks
 * and passed through a check state.
 */
interface ScriptGenerationCheck : Check {

    fun getCheckScript(): String
}
