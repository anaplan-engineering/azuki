package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Check

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
class SimpleThenBuilder(private val checks: MutableList<ScriptGenerationCheck> = mutableListOf()): ThenBuilder {
    override fun addCheck(check: Check) {
        checks.add(check as? ScriptGenerationCheck ?: throw IllegalArgumentException("$check is not a scriptgen check"))
    }

    override fun build() = checks
}
