package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.SendTransferBehaviour

class SendTransferAction(
    private val transferName: String,
) : ConcreteWorldAction, SendTransferBehaviour() {

    override fun act(animation: ConcreteWorldAnimation) {
        animation.sendTransfer(transferName)
    }

}
