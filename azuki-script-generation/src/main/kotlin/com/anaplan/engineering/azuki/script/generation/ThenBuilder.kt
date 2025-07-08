package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import org.slf4j.LoggerFactory

/**
 * Builder for the contents of a 'then' block in a generated script.
 *
 * Implementations of this interface receive the low-level checks passed into
 * script generation from the scenario or system.  They can group together
 * checks, rewrite them, reorder them, and perform other such preprocessing,
 * to reconstitute the high-level DSL.
 */
interface ThenBuilder {
    fun addCheck(check: Check)

    fun build(): List<ScriptGenerationCheck>
}

fun ThenBuilder.addChecks(checks: List<Check>) {
    checks.forEach { addCheck(it) }
}

/**
 * Then-block builder that passes through checks with no processing.
 */
class SimpleThenBuilder(private val checks: MutableList<ScriptGenerationCheck> = mutableListOf()) : ThenBuilder {
    override fun addCheck(check: Check) {
        when (check) {
            is ScriptGenerationCheck -> checks.add(check)
            is UnsupportedCheck -> Log.debug("Tried to emit unsupported check, ignored")
            else -> throw IllegalArgumentException("$check is not a scriptgen check")
        }
    }

    override fun build() = checks

    companion object {
        private val Log = LoggerFactory.getLogger(SimpleThenBuilder::class.java)
    }
}
