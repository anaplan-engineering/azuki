package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AbortTransferBehaviour

class AbortTransferAction(
    private val transferName: String,
) : AbstactWorldAction, AbortTransferBehaviour() {

    override fun act(animation: AbstractWorldAnimation) {
        animation.abortTransfer(transferName)
    }

}
