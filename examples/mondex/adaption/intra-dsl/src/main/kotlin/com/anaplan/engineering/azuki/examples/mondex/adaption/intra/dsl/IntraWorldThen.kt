package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl

import com.anaplan.engineering.azuki.core.dsl.Then
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.check.PurseChecks
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.check.WorldChecks
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl.CheckBlock

class IntraWorldThen(checkFactory: IntraWorldCheckFactory) :
    CheckBlock(checkFactory),
    Then<IntraWorldCheckFactory>,
    WorldChecks,
    PurseChecks {

    override fun worldHasTotalBalance(worldName: String, balance: Int) {
        add(checkFactory.world.hasTotalBalance(worldName, balance))
    }

    override fun worldHasTotalLost(worldName: String, lost: Int) {
        add(checkFactory.world.hasTotalLost(worldName, lost))
    }

    override fun worldHasTotalValue(worldName: String, value: Int) {
        add(checkFactory.world.hasTotalValue(worldName, value))
    }

    override fun worldHasPurse(worldName: String, purseName: String) {
        add(checkFactory.world.hasPurse(worldName, purseName))
    }

    override fun purseHasBalance(purseName: String, balance: Int) {
        add(checkFactory.purse.hasBalance(purseName, balance))
    }

    override fun purseHasLost(purseName: String, lost: Int) {
        add(checkFactory.purse.hasLost(purseName, lost))
    }

    override fun purseExLogContains(purseName: String, transferName: String) {
        add(checkFactory.purse.exLogContains(purseName, transferName))
    }

}
