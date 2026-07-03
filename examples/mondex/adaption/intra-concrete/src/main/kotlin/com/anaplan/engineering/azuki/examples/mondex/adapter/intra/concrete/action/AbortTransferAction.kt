package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AbortTransferBehaviour

class AbortTransferAction(
    private val transferName: String,
) : ConcreteWorldAction, AbortTransferBehaviour() {

    override fun act(animation: ConcreteWorldAnimation) {
        animation.abortTransfer(transferName)
    }

}
