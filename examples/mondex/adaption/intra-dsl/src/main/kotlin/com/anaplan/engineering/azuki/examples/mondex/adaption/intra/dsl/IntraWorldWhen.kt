package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action.PurseActions
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action.TransferActions
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.action.WorldActions
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.dsl.dsl.ActionBlock
import java.util.UUID

class IntraWorldWhen(actionFactory: IntraWorldActionFactory<*>) :
    ActionBlock(actionFactory),
    When<IntraWorldActionFactory<*>>,
    WorldActions,
    PurseActions,
    TransferActions {

    override fun removeAPurse(worldName: String, purseName: String) {
        TODO("Not yet implemented")
    }

    override fun addANewPurse(worldName: String, purseName: String) {
        add(actionFactory.purse.create(purseName, 0))
        add(actionFactory.world.addPurse(worldName, purseName))
    }

    override fun makeATransfer(fromPurse: String, toPurse: String, value: Int, successful: Boolean) {
        val name = UUID.randomUUID().toString()
        add(listOf(actionFactory.transfer.create(name, fromPurse, toPurse, value),
                actionFactory.transfer.request(name),
                actionFactory.transfer.send(name)))
        if (successful) {
            add(actionFactory.transfer.acknowledge(name))
        } else {
            add(actionFactory.transfer.abort(name))
        }
    }

    override fun createTransfer(worldName: String?, transferName: String, fromPurse: String, toPurse: String, amount: Int) {
        add(actionFactory.transfer.create(transferName, fromPurse, toPurse, amount))
        if (worldName != null) {
            add(actionFactory.world.addTransfer(worldName, transferName))
        }
    }

    override fun requestTransfer(transferName: String) {
        add(actionFactory.transfer.request(transferName))
    }

    override fun sendTransfer(transferName: String) {
        add(actionFactory.transfer.send(transferName))
    }

    override fun acknowledgeTransfer(transferName: String) {
        add(actionFactory.transfer.acknowledge(transferName))
    }

    override fun abortTransfer(transferName: String) {
        add(actionFactory.transfer.abort(transferName))
    }


}
