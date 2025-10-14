package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck

/**
 * State used to compose together checks before script generation.
 *
 * While some checks are directly suitable for generation, efficiency and DSL support concerns mean that others need to
 * be merged, rewritten, or otherwise processed in advance.  A check state permits this.
 */
interface ScriptGenerationCheckState {

    /**
     * Adds a check directly to the state's output, without performing any composition.
     */
    fun addCheck(check: ScriptGenerationCheck)

    fun addChecks(checks: Iterable<ScriptGenerationCheck>) {
        checks.forEach { addCheck(it) }
    }

    fun unsupportedCheck()

    fun getChecks(): List<ScriptGenerationCheck>
}

/**
 * Check state with most of the common functionality already provided.
 */
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
