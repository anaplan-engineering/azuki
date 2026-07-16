package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.WorldAnimation
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.action.CreatePurseDeclarableAction

class CreatePurseAction(purseName: String, balance: Int) :
    CreatePurseDeclarableAction(purseName, balance),
    WorldAction {

    override fun act(animation: WorldAnimation<*,*>) {
        throw LateDetectUnsupportedActionException("Cannot add new purses to concrete world")
    }


}
