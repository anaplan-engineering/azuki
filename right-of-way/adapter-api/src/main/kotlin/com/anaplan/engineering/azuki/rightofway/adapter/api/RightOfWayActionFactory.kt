package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface RightOfWayActionFactory : ActionFactory {
    val airspace: AirspaceActionFactory
}

interface AirspaceActionFactory {
    fun start(airspaceName: String, delta_o: Double, delta_c: Double, theta_h: Double, opened: Boolean): Action
    fun save(airspaceName: String): Action
    fun unload(airspaceName: String): Action
    fun load(airspaceName: String): Action
    fun setAirspace(airspaceName: String, opened: Boolean): Action
    //TODO LF: perhaps remove aircraft name and identify on position alone?
    fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft): Action
}
