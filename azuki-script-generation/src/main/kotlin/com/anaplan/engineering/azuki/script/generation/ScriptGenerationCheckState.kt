package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck

/**
 * State used to build a 'then' block out of a stream of low-level generator or generable checks.
 */
interface ScriptGenerationCheckState {

    /**
     * Pass through a check that doesn't need any special handling in the check state.
     */
    fun addCheck(check: ScriptGenerationCheck)

    fun unsupportedCheck()

    fun getChecks(): List<ScriptGenerationCheck>
}

interface ScriptGenerationCheckStateFactory<S: ScriptGenerationCheckState> {

    fun create(): S
}

class ScriptGenerationCheckStateBuilder<S : ScriptGenerationCheckState>(private val factory: ScriptGenerationCheckStateFactory<S>) {

    fun build(checks: List<Check>): List<ScriptGenerationCheck> {
        val checkState = factory.create()
        checks.forEach {
            @Suppress("UNCHECKED_CAST")
            when(it) {
                is ScriptGenerationCheck -> checkState.addCheck(it)
                is GenerableCheck<*> -> (it as GenerableCheck<S>).generate(checkState)
                is UnsupportedCheck -> checkState.unsupportedCheck()
                else -> throw IllegalArgumentException("invalid check for script generation: $it")
            }
        }
        return checkState.getChecks()
    }
}

/**
 * Check state for script generation adapters that don't need generable checks.
 */
class SimpleScriptGenerationCheckState(private val checks: MutableList<ScriptGenerationCheck> = mutableListOf()):
    ScriptGenerationCheckState {

    override fun addCheck(check: ScriptGenerationCheck) {
        checks.add(check)
    }

    override fun unsupportedCheck() =
        throw IllegalArgumentException("unsupported check detected")

    override fun getChecks(): List<ScriptGenerationCheck> = checks

    object Factory: ScriptGenerationCheckStateFactory<SimpleScriptGenerationCheckState> {
        override fun create() = SimpleScriptGenerationCheckState()
    }
}

/**
 * A check that can't be generated directly, but instead needs to be fed into a check state.
 */
interface GenerableCheck<S: ScriptGenerationCheckState> : Check {
    fun generate(state: S)
}
