package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AcknowledgeTransferBehaviour

class AcknowledgeTransferAction(
    private val transferName: String,
) : ConcreteWorldAction, AcknowledgeTransferBehaviour() {

    override fun act(animation: ConcreteWorldAnimation) {
        animation.acknowledgeTransfer(transferName)
    }

}
