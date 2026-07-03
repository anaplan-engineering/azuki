package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.PurseActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.TransferActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.WorldActionFactory

object AbstractWorldActionFactory : IntraWorldActionFactory<AbstactWorldAction> {
    override val purse = AbstractWorldPurseActionFactory
    override val world = AbstractWorldWorldActionFactory
    override val transfer = AbstractWorldTransferActionFactory
}

object AbstractWorldTransferActionFactory : TransferActionFactory {

    override fun create(transferName: String, fromPurse: String, toPurse: String, amount: Int) =
        CreateTransferAction(transferName, fromPurse, toPurse, amount)

    override fun request(transferName: String) = RequestTransferAction(transferName)
    override fun send(transferName: String) = SendTransferAction(transferName)
    override fun acknowledge(transferName: String) = AcknowledgeTransferAction(transferName)
    override fun abort(transferName: String) = AbortTransferAction(transferName)

}

object AbstractWorldPurseActionFactory : PurseActionFactory {
    override fun create(purseName: String, balance: Int) = CreatePurseAction(purseName, balance)
}

object AbstractWorldWorldActionFactory : WorldActionFactory {
    override fun create(worldName: String) = CreateWorldAction(worldName)
    override fun addPurse(worldName: String, purseName: String) = AddPurseToWorldAction(worldName, purseName)
    override fun addTransfer(worldName: String, transferName: String) = AddTransferToWorldAction(worldName, transferName)
}

interface AbstactWorldAction : Action {
    fun act(animation: AbstractWorldAnimation)
}

