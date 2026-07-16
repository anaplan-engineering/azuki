package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.check

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.PurseHasBalanceCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.PurseHasLostCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.WorldHasPurseCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.WorldHasTotalBalanceCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.WorldHasTotalLostCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.PurseCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.WorldCheckFactory
import com.anaplan.engineering.kazuki.core.*
import org.slf4j.LoggerFactory

object ConcreteWorldCheckFactory : IntraWorldCheckFactory {
    override val purse = ConcreteWorldPurseCheckFactory
    override val world = ConcreteWorldWorldCheckFactory
}

object ConcreteWorldPurseCheckFactory : PurseCheckFactory {
    override fun hasBalance(purseName: String, balance: Int) = PurseHasBalanceCheck(purseName, balance)
    //TODO this is only possible through the retrieve
    override fun hasLost(purseName: String, lost: Int) = PurseHasLostCheck(purseName, lost) // TODO rewrite
}

object ConcreteWorldWorldCheckFactory : WorldCheckFactory {
    override fun hasPurse(worldName: String, purseName: String) = WorldHasPurseCheck(worldName, purseName)
    // TODO Checks that always pass - need fixing with retrieve
    override fun hasTotalBalance(worldName: String, balance: Int) = WorldHasTotalBalanceCheck(worldName, balance)
    override fun hasTotalLost(worldName: String, lost: Int) = WorldHasTotalLostCheck(worldName, lost)
}

interface ConcreteWorldCheck : Check {
    fun check(animation: ConcreteWorldAnimation): Boolean

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
        private val Log = LoggerFactory.getLogger(ConcreteWorldCheck::class.java)
    }

}
