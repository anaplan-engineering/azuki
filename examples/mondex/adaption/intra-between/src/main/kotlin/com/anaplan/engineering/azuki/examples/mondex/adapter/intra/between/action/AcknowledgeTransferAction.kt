package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AcknowledgeTransferBehaviour

class AcknowledgeTransferAction(
    private val transferName: String,
) : BetweenWorldAction, AcknowledgeTransferBehaviour() {

    override fun act(animation: BetweenWorldAnimation) {
        animation.acknowledgeTransfer(transferName)
    }

}
