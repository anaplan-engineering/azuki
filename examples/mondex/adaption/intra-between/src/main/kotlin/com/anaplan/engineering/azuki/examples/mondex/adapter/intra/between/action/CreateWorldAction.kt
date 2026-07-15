package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateWorldDeclarableAction

class CreateWorldAction(worldName: String) :
    CreateWorldDeclarableAction(worldName),
    BetweenWorldAction {

    override fun act(animation: BetweenWorldAnimation) {
        throw LateDetectUnsupportedActionException("Cannot add new worlds to concrete system")
    }

}
