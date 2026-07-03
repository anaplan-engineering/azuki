package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.ConcreteWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.AddPurseToWorldDeclarableAction

class AddPurseToWorldAction(worldName: String, purseName: String) :
    AddPurseToWorldDeclarableAction(worldName, purseName),
    ConcreteWorldAction {

    override fun act(animation: ConcreteWorldAnimation) {
        throw LateDetectUnsupportedActionException("Cannot add new purses to concrete world")
    }


}
