package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.TransferData
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateTransferDeclarableAction

class CreateTransferAction(
    transferName: String,
    fromPurse: String,
    toPurse: String,
    amount: Int
) : CreateTransferDeclarableAction(transferName, fromPurse, toPurse, amount),
    BetweenWorldAction {


    override fun act(animation: BetweenWorldAnimation) {
        animation.createTransfer(TransferData(transferName, fromPurse, toPurse, amount, TransferData.Status.Created))
    }

}
