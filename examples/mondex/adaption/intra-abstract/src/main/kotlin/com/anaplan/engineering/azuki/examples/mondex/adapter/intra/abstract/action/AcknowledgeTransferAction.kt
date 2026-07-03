package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AcknowledgeTransferBehaviour

class AcknowledgeTransferAction(
    private val transferName: String,
) : AbstactWorldAction, AcknowledgeTransferBehaviour() {

    override fun act(animation: AbstractWorldAnimation) {
        animation.acknowledgeTransfer(transferName)
    }

}
