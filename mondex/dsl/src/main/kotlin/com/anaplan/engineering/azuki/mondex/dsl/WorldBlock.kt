package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexActionFactory

@ScenarioDsl
class WorldBlock {

    private val authPurses = HashMap<String, Pair<ULong, ULong>>()

    fun getAuthPurses(): Map<String, Pair<ULong, ULong>> = authPurses

    fun personWithPurse(personName: String, balance: Int, lost: Int) {
        require(balance >= 0 && lost >= 0) { "Purse values must be zero or greater than zero." }
        require(personName !in authPurses) { "Person must be new" }
        authPurses[personName] = (balance.toULong() to lost.toULong())
    }
}
