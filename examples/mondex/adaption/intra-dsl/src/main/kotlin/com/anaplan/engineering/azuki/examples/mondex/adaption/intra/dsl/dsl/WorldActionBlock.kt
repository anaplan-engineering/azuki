package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl

import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition.PurseDefinition
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.definition.TransferDefinition

class WorldActionBlock(actionFactory: IntraWorldActionFactory<*>, val worldName: String?) :
    ActionBlock(actionFactory),
    PurseDefinition,
    TransferDefinition {

    init {
        if (worldName != null) {
            add(actionFactory.world.create(worldName))
        }
    }

    override fun thereIsAPurse(purseName: String, balance: Int) {
        add(actionFactory.purse.create(purseName, balance))
        if (worldName != null) {
            add(actionFactory.world.addPurse(worldName, purseName))
        }
    }

    override fun thereIsATransfer(
        transferName: String,
        fromPurse: String,
        toPurse: String,
        amount: Int
    ) {
        add(actionFactory.transfer.create(transferName, fromPurse, toPurse, amount))
        if (worldName != null) {
            add(actionFactory.world.addTransfer(worldName, transferName))
        }
    }




}
