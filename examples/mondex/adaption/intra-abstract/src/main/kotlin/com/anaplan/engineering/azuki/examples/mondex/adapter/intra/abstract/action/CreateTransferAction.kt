package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.TransferData
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.TransferData.Status
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateTransferDeclarableAction

class CreateTransferAction(
    transferName: String,
    fromPurse: String,
    toPurse: String,
    amount: Int
) : CreateTransferDeclarableAction(transferName, fromPurse, toPurse, amount),
    AbstactWorldAction {


    override fun act(animation: AbstractWorldAnimation) {
        animation.createTransfer(TransferData(transferName, fromPurse, toPurse, amount, Status.Created))
    }

}
