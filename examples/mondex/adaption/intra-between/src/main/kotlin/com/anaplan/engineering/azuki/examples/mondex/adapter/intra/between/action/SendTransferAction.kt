package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.SendTransferBehaviour

class SendTransferAction(
    private val transferName: String,
) : BetweenWorldAction, SendTransferBehaviour() {

    override fun act(animation: BetweenWorldAnimation) {
        animation.sendTransfer(transferName)
    }

}
