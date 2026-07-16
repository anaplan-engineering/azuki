package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.SendTransferBehaviour

class SendTransferAction(
    private val transferName: String,
) : WorldAction, SendTransferBehaviour() {

    override fun act(animation: WorldAnimation<*,*>) {
        animation.sendTransfer(transferName)
    }

}
