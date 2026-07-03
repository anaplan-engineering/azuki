package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.PurseCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.WorldCheckFactory
import com.anaplan.engineering.kazuki.core.*
import org.slf4j.LoggerFactory

object AbstractWorldCheckFactory : IntraWorldCheckFactory {
    override val purse = AbstractWorldPurseCheckFactory
    override val world = AbstractWorldWorldCheckFactory
}

object AbstractWorldPurseCheckFactory : PurseCheckFactory {
    override fun hasBalance(purseName: String, balance: Int) = PurseHasBalanceCheck(purseName, balance)
    override fun hasLost(purseName: String, lost: Int) = PurseHasLostCheck(purseName, lost)
}

object AbstractWorldWorldCheckFactory : WorldCheckFactory {
    override fun hasTotalValue(worldName: String, value: Int) = WorldHasTotalValueCheck(worldName, value)

    override fun hasPurse(worldName: String, purseName: String) = WorldHasPurseCheck(worldName, purseName)

}

interface AbstractWorldCheck : Check {
    fun check(animation: AbstractWorldAnimation): Boolean

    fun checkFailure(message: String? = null, fn: () -> Any): Boolean = try {
        fn()
        Log.error("${prefix(message)} failed, operation succeeded")
        false
    } catch (e: ConditionFailure) {
        Log.debug("${prefix(message)} passed, caught ${e.message}")
        true
    }

    fun checkTrue(actual: Boolean, message: String? = null): Boolean {
        if (actual) {
            Log.debug("${prefix(message)} passed")
        } else {
            Log.error("${prefix(message)} failed\nExpected:\ntrue\nActual:\n$actual")
        }
        return actual
    }

    fun <T> checkEquals(actual: T, expected: T, message: String? = null): Boolean {
        val result = actual == expected
        if (result) {
            Log.debug("${prefix(message)} passed")
        } else {
            Log.error("${prefix(message)} failed\nExpected:\n${expected.prettyOrDefault()}\nActual:\n${actual.prettyOrDefault()}")
        }
        return result
    }

    private fun prefix(message: String?) =
        "Check '${javaClass.simpleName}' ${if (message == null) "" else "[$message]"}"

    companion object {
        private val Log = LoggerFactory.getLogger(AbstractWorldCheck::class.java)
    }

}
