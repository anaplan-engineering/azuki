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

fun interface ScriptGenerationCheckStateFactory<S : ScriptGenerationCheckState, E : ScriptGenerationEnvironment> {

    fun create(environment: E): S
}

class ScriptGenerationCheckStateBuilder<S : ScriptGenerationCheckState, E : ScriptGenerationEnvironment>(private val factory: ScriptGenerationCheckStateFactory<S, E>) {

    fun build(environment: E, checks: List<Check>): List<ScriptGenerationCheck> {
        val checkState = factory.create(environment)

        checks.forEach {
            if (it !is ScriptGenerationCheck && it !is ComposableCheck<*> && it !is UnsupportedCheck) throw IllegalArgumentException(
                "unsupported check: $it")
        }

        val passThrough = checks.filterIsInstance<ScriptGenerationCheck>()
        val toCompose = checks.filterIsInstance<ComposableCheck<S>>()
        val unsupported = checks.filterIsInstance<UnsupportedCheck>()

        checkState.addChecks(passThrough)
        toCompose.forEach { it.composeInto(checkState) }
        repeat(unsupported.size) { checkState.unsupportedCheck() }

        return checkState.getChecks()
    }
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

/**
 * A check that can't be generated directly, but instead needs to be composed using a check state.
 */
interface ComposableCheck<S : ScriptGenerationCheckState> : Check {

    fun composeInto(state: S)
}
