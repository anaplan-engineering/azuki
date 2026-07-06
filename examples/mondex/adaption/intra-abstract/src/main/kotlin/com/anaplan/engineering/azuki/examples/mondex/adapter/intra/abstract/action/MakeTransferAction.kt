package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.TransferData
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.TransferData.Status
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateTransferDeclarableAction

//LF QST: Here we "enact" whole protocol sequence at the DSL level, rather than at animation level
//        assuming this is a kind of create transfer declaration
//        assuming the transfer name here is irrelevant to the user too, hence construct it uniquely
class MakeTransferAction(
    fromPurse: String,
    toPurse: String,
    amount: Int,
    private val successful: Boolean,
) : CreateTransferDeclarableAction(
    nextTransferName(fromPurse, toPurse, amount),
    fromPurse,
    toPurse,
    amount,
), AbstactWorldAction {

    companion object {
        private var nextCount = 0

        fun resetTransferNameCounter() {
            nextCount = 0
        }

        private fun nextTransferName(from: String, to: String, amount: Int): String =
            "${from}_${to}_${amount}_id${nextCount++}"
    }

    override fun act(animation: AbstractWorldAnimation) {
        animation.createTransfer(TransferData(transferName, fromPurse, toPurse, amount, Status.Created))
        animation.requestTransfer(transferName)
        animation.sendTransfer(transferName)
        if (successful) {
            animation.acknowledgeTransfer(transferName)
        } else {
            animation.abortTransfer(transferName)
        }
    }

}
