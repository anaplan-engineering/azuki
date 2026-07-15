package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AbortTransferBehaviour

class AbortTransferAction(
    private val transferName: String,
) : BetweenWorldAction, AbortTransferBehaviour() {

    override fun act(animation: BetweenWorldAnimation) {
        animation.abortTransfer(transferName)
    }

}
