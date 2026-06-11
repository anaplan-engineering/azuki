package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment

class KazukiActionFactory : RightOfWayActionFactory {
    override val airspace = KazukiAirspaceActionFactory
}

object KazukiAirspaceActionFactory : AirspaceActionFactory {
    override fun start(airspaceName: String) = StartAirspaceAction(airspaceName)
    override fun addAircraft(airspaceName: String, aircraftName: String, position: Position, velocity: Velocity): Action =
        CreateAircraftAction(airspaceName, aircraftName, position, velocity)

    // Kazuki doesn't support file-related actions
    override fun save(airspaceName: String) = UnsupportedAction
    override fun close(airspaceName: String) = UnsupportedAction
    override fun load(airspaceName: String) = UnsupportedAction
}

interface KazukiAction : Action {
    fun act(env: ExecutionEnvironment)
}
