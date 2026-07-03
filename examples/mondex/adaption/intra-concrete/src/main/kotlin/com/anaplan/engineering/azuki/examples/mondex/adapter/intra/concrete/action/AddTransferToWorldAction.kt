package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.AddTransferToWorldDeclarableAction

class AddTransferToWorldAction(worldName: String, purseName: String) :
    AddTransferToWorldDeclarableAction(worldName, purseName),
    ConcreteWorldAction {

    override fun act(animation: ConcreteWorldAnimation) {
        require(worldName == animation.worldName)
        // Don't need to do anything else
    }


}
