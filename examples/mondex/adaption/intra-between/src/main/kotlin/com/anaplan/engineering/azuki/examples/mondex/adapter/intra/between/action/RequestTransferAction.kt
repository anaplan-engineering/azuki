package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.RequestTransferBehaviour

class RequestTransferAction(
    private val transferName: String,
) : BetweenWorldAction, RequestTransferBehaviour() {

    override fun act(animation: BetweenWorldAnimation) {
        animation.requestTransfer(transferName)
    }

}
