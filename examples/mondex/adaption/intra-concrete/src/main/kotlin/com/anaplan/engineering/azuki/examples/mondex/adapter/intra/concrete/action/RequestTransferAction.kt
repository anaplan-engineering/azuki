package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.RequestTransferBehaviour

class RequestTransferAction(
    private val transferName: String,
) : ConcreteWorldAction, RequestTransferBehaviour() {

    override fun act(animation: ConcreteWorldAnimation) {
        animation.requestTransfer(transferName)
    }

}
