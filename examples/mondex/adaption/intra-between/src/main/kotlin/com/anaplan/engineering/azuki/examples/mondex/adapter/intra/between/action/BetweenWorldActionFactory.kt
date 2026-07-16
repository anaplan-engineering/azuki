package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.PurseActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.TransferActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.WorldActionFactory

object BetweenWorldActionFactory : IntraWorldActionFactory<WorldAction> {
    override val purse = BetweenWorldPurseActionFactory
    override val world = BetweenWorldWorldActionFactory
    override val transfer = BetweenWorldTransferActionFactory
}

object BetweenWorldTransferActionFactory : TransferActionFactory {

    override fun create(transferName: String, fromPurse: String, toPurse: String, amount: Int) =
        CreateTransferAction(transferName, fromPurse, toPurse, amount)
    override fun request(transferName: String) = RequestTransferAction(transferName)
    override fun send(transferName: String) = SendTransferAction(transferName)
    override fun acknowledge(transferName: String) = AcknowledgeTransferAction(transferName)
    override fun abort(transferName: String) = AbortTransferAction(transferName)
}

object BetweenWorldPurseActionFactory : PurseActionFactory {
    override fun create(purseName: String, balance: Int) = CreatePurseAction(purseName, balance)
}

object BetweenWorldWorldActionFactory : WorldActionFactory {
    override fun create(worldName: String) = CreateWorldAction(worldName)
    override fun addPurse(worldName: String, purseName: String) = AddPurseToWorldAction(worldName,purseName)
    override fun addTransfer(worldName: String, transferName: String) = AddTransferToWorldAction(worldName, transferName)
}

interface WorldAction : Action {
    fun act(animation: WorldAnimation<*,*>)
}
