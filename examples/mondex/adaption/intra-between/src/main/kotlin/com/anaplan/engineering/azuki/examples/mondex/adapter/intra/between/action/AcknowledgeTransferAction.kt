package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AcknowledgeTransferBehaviour

class AcknowledgeTransferAction(
    private val transferName: String,
) : WorldAction, AcknowledgeTransferBehaviour() {

    override fun act(animation: WorldAnimation<*,*>) {
        animation.acknowledgeTransfer(transferName)
    }

}
