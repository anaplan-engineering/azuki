package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.RegardlessOf
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory

class RightOfWayRegardlessOf(private val actionFactory: RightOfWayActionFactory) : RegardlessOf<RightOfWayActionFactory> {

    fun saveAirspace(airspaceName: String) {
        actionList.add(actionFactory.airspace.save(airspaceName))
    }

    fun unloadAirspace(airspaceName: String) {
        actionList.add(actionFactory.airspace.unload(airspaceName))
    }

    fun loadAirspace(airspaceName: String) {
        actionList.add(actionFactory.airspace.load(airspaceName))
    }

    fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) {
        actionList.add(actionFactory.airspace.addAircraft(airspaceName, aircraftName, aircraft))
    }

    private val actionList = mutableListOf<Action>()

    override fun actions(): List<Action> = actionList

}
