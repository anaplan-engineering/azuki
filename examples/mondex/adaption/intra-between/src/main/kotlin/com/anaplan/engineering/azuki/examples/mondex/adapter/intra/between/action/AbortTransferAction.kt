package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.AbortTransferBehaviour

class AbortTransferAction(
    private val transferName: String,
) : WorldAction, AbortTransferBehaviour() {

    override fun act(animation: WorldAnimation<*, *>) {
        animation.abortTransfer(transferName)
    }

}
