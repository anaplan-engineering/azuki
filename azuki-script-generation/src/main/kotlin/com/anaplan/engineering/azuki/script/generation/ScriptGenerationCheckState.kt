package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck

/**
 * State used to build a 'then' block out of a stream of low-level generator or generable checks.
 */
interface ScriptGenerationCheckState {

    /**
     * Adds a check to the set of checks to be output directly into the resulting 'then' block.
     */
    fun addCheck(check: ScriptGenerationCheck)

    fun addChecks(checks: Iterable<ScriptGenerationCheck>) {
        checks.forEach { addCheck(it) }
    }

    fun unsupportedCheck()

    fun getChecks(): List<ScriptGenerationCheck>
}

fun interface ScriptGenerationCheckStateFactory<S : ScriptGenerationCheckState> {

    fun create(): S
}

class ScriptGenerationCheckStateBuilder<S : ScriptGenerationCheckState>(private val factory: ScriptGenerationCheckStateFactory<S>) {

    fun build(checks: List<Check>): List<ScriptGenerationCheck> {
        val checkState = factory.create()

        checks.forEach {
            if (it !is ScriptGenerationCheck && it !is GenerableCheck<*> && it !is UnsupportedCheck) throw IllegalArgumentException(
                "unsupported check: $it")
        }

        val passThrough = checks.filterIsInstance<ScriptGenerationCheck>()
        val toGenerate = checks.filterIsInstance<GenerableCheck<S>>()
        val unsupported = checks.filterIsInstance<UnsupportedCheck>()

        checkState.addChecks(passThrough)
        toGenerate.forEach { it.generate(checkState) }
        repeat(unsupported.size) { checkState.unsupportedCheck() }

        return checkState.getChecks()
    }
}

abstract class AbstractScriptGenerationCheckState : ScriptGenerationCheckState {

    protected val finishedChecks: MutableList<ScriptGenerationCheck> = mutableListOf()

    override fun addCheck(check: ScriptGenerationCheck) {
        finishedChecks.add(check)
    }

    override fun addChecks(checks: Iterable<ScriptGenerationCheck>) {
        finishedChecks.addAll(checks)
    }

    override fun unsupportedCheck() = throw IllegalArgumentException("unsupported check detected")
}

/**
 * Check state for script generation adapters that don't need generable checks.
 */
class SimpleScriptGenerationCheckState : AbstractScriptGenerationCheckState() {

    override fun getChecks(): List<ScriptGenerationCheck> = finishedChecks
}

/**
 * A check that can't be generated directly, but instead needs to be fed into a check state.
 */
interface GenerableCheck<S : ScriptGenerationCheckState> : Check {

    fun generate(state: S)
}
