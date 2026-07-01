package com.anaplan.engineering.azuki.mondex.dsl.block

import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.ConPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.PayDetails
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.api.Status

abstract class WorldBlock(actionFactory: MondexActionFactory<*>) {
    protected val purses = mutableMapOf<String, Purse>()

    //internal fun purses
}

@ScenarioDsl
class AbWorldBlock(actionFactory: MondexActionFactory<*>) : WorldBlock(actionFactory) {

    fun thereIsAPurse(purseName: String, balance: ULong, lost: ULong = 0UL) {
        require(purseName !in purses) { "Purse $purseName already exists" }
        purses[purseName] = AbPurse(balance, lost)
    }

    val abPurses = purses.mapValues { it.value as AbPurse }
}

@ScenarioDsl
class ConWorldBlock(actionFactory: MondexActionFactory<*>) : WorldBlock(actionFactory) {

    /**
     * Creates a concrete purse with given balance, failed transfers log, name,
     * sequence number for next transaction, payment details for the upcoming transaction
     * (or null if just creating purses), and status of current transaction.
     */
    fun thereIsAPurse(
        purseName: String, balance: ULong, exLog: Set<PayDetails> = emptySet(),
        nextSeqNo: ULong = 0UL,
        pdAuth: PayDetails? = null,
        status: Status = Status.eaFrom
    ) {
        require(purseName !in purses) { "Purse $purseName already exists" }
        purses[purseName] = ConPurse(balance, exLog, purseName, nextSeqNo, pdAuth, status)
    }

    val conPurses = purses.mapValues { it.value as ConPurse }
}
