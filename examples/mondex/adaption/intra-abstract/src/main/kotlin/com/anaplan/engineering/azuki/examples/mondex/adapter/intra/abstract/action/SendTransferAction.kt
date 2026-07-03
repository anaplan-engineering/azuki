package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.SendTransferBehaviour

class SendTransferAction(
    private val transferName: String,
) : AbstactWorldAction, SendTransferBehaviour() {

    override fun act(animation: AbstractWorldAnimation) {
        animation.sendTransfer(transferName)
    }

}
