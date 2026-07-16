package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.AddPurseToWorldDeclarableAction

class AddPurseToWorldAction(worldName: String, purseName: String) :
    AddPurseToWorldDeclarableAction(worldName, purseName),
    WorldAction {

    override fun act(animation: WorldAnimation<*,*>) {
        throw LateDetectUnsupportedActionException("Cannot add new purses to concrete world")
    }


}
