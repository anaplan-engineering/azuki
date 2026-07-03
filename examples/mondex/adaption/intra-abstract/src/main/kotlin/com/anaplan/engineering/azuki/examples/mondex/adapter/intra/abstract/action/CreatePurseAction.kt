package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.AbstractWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreatePurseDeclarableAction

class CreatePurseAction(purseName: String, balance: Int) :
    CreatePurseDeclarableAction(purseName, balance),
    AbstactWorldAction {


    override fun act(animation: AbstractWorldAnimation) {
        throw LateDetectUnsupportedActionException("Cannot add new purses to abstract world")
    }


}
