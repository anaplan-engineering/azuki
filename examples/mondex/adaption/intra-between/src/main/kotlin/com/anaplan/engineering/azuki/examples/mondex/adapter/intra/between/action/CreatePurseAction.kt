package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.BetweenWorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreatePurseDeclarableAction

class CreatePurseAction(purseName: String, balance: Int) :
    CreatePurseDeclarableAction(purseName, balance),
    BetweenWorldAction {

    override fun act(animation: BetweenWorldAnimation) {
        throw LateDetectUnsupportedActionException("Cannot add new purses to concrete world")
    }


}
