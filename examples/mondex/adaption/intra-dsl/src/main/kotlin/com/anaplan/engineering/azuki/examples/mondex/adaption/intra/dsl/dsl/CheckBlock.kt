package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory

abstract class CheckBlock(protected val checkFactory: IntraWorldCheckFactory) {

    private val checkList = mutableListOf<Check>()

    protected fun add(check: Check) = checkList.add(check)

    protected fun add(checks: List<Check>) = checkList.addAll(checks)

    fun checks(): List<Check> = checkList
}
