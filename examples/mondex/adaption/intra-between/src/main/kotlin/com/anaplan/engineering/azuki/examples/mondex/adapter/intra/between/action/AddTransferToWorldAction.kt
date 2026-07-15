package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.AddTransferToWorldDeclarableAction

class AddTransferToWorldAction(worldName: String, purseName: String) :
    AddTransferToWorldDeclarableAction(worldName, purseName),
    BetweenWorldAction {

    override fun act(animation: BetweenWorldAnimation) {
        require(worldName == animation.worldName)
        // Don't need to do anything else
    }


}
