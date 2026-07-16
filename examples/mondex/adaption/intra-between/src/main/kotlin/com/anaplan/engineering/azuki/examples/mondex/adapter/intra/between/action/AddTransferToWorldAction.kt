package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.AddTransferToWorldDeclarableAction

class AddTransferToWorldAction(worldName: String, purseName: String) :
    AddTransferToWorldDeclarableAction(worldName, purseName),
    WorldAction {

    override fun act(animation: WorldAnimation<*,*>) {
        require(worldName == animation.worldName)
        // Don't need to do anything else
    }


}
