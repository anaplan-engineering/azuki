package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.RequestTransferBehaviour

class RequestTransferAction(
    private val transferName: String,
) : AbstactWorldAction, RequestTransferBehaviour() {

    override fun act(animation: AbstractWorldAnimation) {
        animation.requestTransfer(transferName)
    }

}
