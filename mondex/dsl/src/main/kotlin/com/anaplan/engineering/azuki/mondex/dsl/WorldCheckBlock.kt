package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory

@ScenarioDsl
class WorldCheckBlock(
    private val checkFactory: MondexCheckFactory
) {
    private val checkList = mutableListOf<Check>()
    private val authPurses = HashMap<String, Pair<ULong, ULong>>()

    fun checks(): List<Check> = checkList
    fun getAuthPurses(): Map<String, Pair<ULong, ULong>> = authPurses

    fun personWithPurse(personName: String, balance: Int, lost: Int) {
        require(balance >= 0 && lost >= 0) { "Balance and lost must be greater than or equal to 0" }
        checkList.add(checkFactory.purse.purseExists(personName, balance.toULong(), lost.toULong(), true))
        authPurses[personName] = (balance.toULong() to lost.toULong())
    }

}
