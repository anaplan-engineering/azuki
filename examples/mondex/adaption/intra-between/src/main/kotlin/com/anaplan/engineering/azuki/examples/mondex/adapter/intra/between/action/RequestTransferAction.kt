package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.RequestTransferBehaviour

class RequestTransferAction(
    private val transferName: String,
) : WorldAction, RequestTransferBehaviour() {

    override fun act(animation: WorldAnimation<*,*>) {
        animation.requestTransfer(transferName)
    }

}
