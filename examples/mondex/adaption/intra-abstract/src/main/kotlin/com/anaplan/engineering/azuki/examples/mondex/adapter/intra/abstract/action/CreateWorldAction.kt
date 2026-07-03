package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateWorldDeclarableAction

class CreateWorldAction(worldName: String) :
    CreateWorldDeclarableAction(worldName),
    AbstactWorldAction {

    override fun act(animation: AbstractWorldAnimation) {
        throw LateDetectUnsupportedActionException("Cannot add new worlds to abstract system")
    }

}
