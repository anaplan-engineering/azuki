package com.anaplan.engineering.azuki.mondex.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.mondex.adapter.api.MondexCheckFactory
import com.anaplan.engineering.azuki.mondex.dsl.check.MondexChecks

class MondexThen(private val checkFactory: MondexCheckFactory) : Then<MondexCheckFactory>,
    MondexChecks {

    private val checkList = mutableListOf<Check>()

    override fun checks() = checkList

    override fun purseExists(personName: String) {
        checkList.add(checkFactory.purse.purseExists(personName, true))
    }

    override fun purseExists(personName: String, balance: Int, lost: Int) {
        require(balance >= 0 && lost >= 0) { "Balance and lost must be greater than or equal to 0" }
        checkList.add(checkFactory.purse.purseExists(personName, balance.toULong(), lost.toULong(), true))
    }

    override fun purseOf(personName: String, init: PurseCheckBlock.() -> Unit) {
        val purseCheckBlock = PurseCheckBlock()
        purseCheckBlock.init()
        checkList.add(checkFactory.purse.purseExists(personName,
            purseCheckBlock.getBalance()!!, purseCheckBlock.getLost()!!, true))
    }

    override fun worldExists(init: WorldCheckBlock.() -> Unit) {
        val worldCheckBlock = WorldCheckBlock(checkFactory)
        worldCheckBlock.init()
        checkList.addAll(worldCheckBlock.checks())
        checkList.add(checkFactory.world.worldExists(worldCheckBlock.getAuthPurses(), true))
    }

    override fun noValueCreation() {
        checkList.add(checkFactory.world.noValueCreation(true))
    }

    override fun allValueAccounted() {
        checkList.add(checkFactory.world.allValueAccounted(true))
    }
}
