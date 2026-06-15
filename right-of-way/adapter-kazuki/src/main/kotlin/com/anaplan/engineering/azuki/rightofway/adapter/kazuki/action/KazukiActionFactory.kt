package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.ExecutionEnvironment

class KazukiActionFactory : RightOfWayActionFactory {
    override val airspace = KazukiAirspaceActionFactory
}

object KazukiAirspaceActionFactory : AirspaceActionFactory {
    override fun start(airspaceName: String) = StartAirspaceAction(airspaceName, true)
    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft): Action =
        CreateAircraftAction(airspaceName, aircraftName, aircraft)

    // Kazuki doesn't support file-related actions
    override fun save(airspaceName: String) = UnsupportedAction
    override fun close(airspaceName: String) = StartAirspaceAction(airspaceName, false)
    override fun load(airspaceName: String) = UnsupportedAction
}

interface KazukiAction : Action {
    fun act(env: ExecutionEnvironment)
}
