package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.TransferData
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.TransferData.Status
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateTransferDeclarableAction

class CreateTransferAction(
    transferName: String,
    fromPurse: String,
    toPurse: String,
    amount: Int
) : CreateTransferDeclarableAction(transferName, fromPurse, toPurse, amount),
    ConcreteWorldAction {


    override fun act(animation: ConcreteWorldAnimation) {
        animation.createTransfer(TransferData(transferName, fromPurse, toPurse, amount, Status.Created))
    }

}
