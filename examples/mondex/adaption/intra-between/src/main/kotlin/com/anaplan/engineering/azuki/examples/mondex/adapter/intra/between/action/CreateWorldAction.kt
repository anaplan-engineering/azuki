package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreateWorldDeclarableAction

class CreateWorldAction(worldName: String) :
    CreateWorldDeclarableAction(worldName),
    WorldAction {

    override fun act(animation: WorldAnimation<*,*>) {
        throw LateDetectUnsupportedActionException("Cannot add new worlds to concrete system")
    }

}
