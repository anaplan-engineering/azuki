package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory

interface RightOfWayActionFactory : ActionFactory {
    val airspace: AirspaceActionFactory
}

interface AirspaceActionFactory {
    fun start(airspaceName: String): Action
    fun save(airspaceName: String): Action
    fun close(airspaceName: String): Action
    fun load(airspaceName: String): Action
    fun addAircraft(airspaceName: String, aircraftName: String, position: Position, velocity: Velocity): Action
}
