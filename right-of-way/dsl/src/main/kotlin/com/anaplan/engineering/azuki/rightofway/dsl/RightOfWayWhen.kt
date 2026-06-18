package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.When
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory

class RightOfWayWhen(val actionFactory: RightOfWayActionFactory): When<RightOfWayActionFactory> {

    fun placeAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) {
        actionList.add(actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft))
    }

    private val actionList = mutableListOf<Action>()

    override fun actions() = actionList

}
