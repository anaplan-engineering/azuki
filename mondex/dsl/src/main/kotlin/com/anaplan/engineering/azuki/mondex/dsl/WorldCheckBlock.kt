package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.ScenarioDsl
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.ConPurse
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.api.WorldLevel

@ScenarioDsl
class WorldCheckBlock(
    private val checkFactory: MondexCheckFactory,
    private val worldLevel: WorldLevel
) {
    private val checkList = mutableListOf<Check>()
    private val authPurses = HashMap<String, Purse>()

    fun checks(): List<Check> = checkList

    fun getAuthPurses(): Map<String, Purse> =
        authPurses.filterValues {
            //it.matchesWorldLevel(worldLevel) }
            when (worldLevel) {
                WorldLevel.ABSTRACT -> it is AbPurse
                WorldLevel.BETWEEN, WorldLevel.CONCRETE -> it is ConPurse
            }
        }

    //LF: same problem here. Might call it person with purse, but needs different 'world views'
    //    will need a ConPurse version
    fun personWithPurse(personName: String, balance: Int, lost: Int) {
        require(balance >= 0 && lost >= 0) { "Balance and lost must be greater than or equal to 0" }
        checkList.add(checkFactory.purse.purseExists(personName, balance.toULong(), lost.toULong(), true))
        authPurses[personName] = when (worldLevel) {
            WorldLevel.ABSTRACT -> AbPurse(balance.toULong(), lost.toULong())
            else -> TODO("ConPurse creation for $worldLevel")
        }
    }

    fun noValueCreation(level: WorldLevel = WorldLevel.ABSTRACT) {

    }

    fun allValuesAccountedFor(level: WorldLevel = WorldLevel.ABSTRACT) {

    }
}
