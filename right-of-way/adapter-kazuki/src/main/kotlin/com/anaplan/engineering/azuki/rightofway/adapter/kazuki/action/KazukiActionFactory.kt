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
    override fun start(airspaceName: String, delta_o: Double, delta_c: Double, theta_h: Double, opened: Boolean) =
        StartAirspaceAction(airspaceName, delta_o, delta_c, theta_h, opened)

    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft): Action =
        CreateAircraftAction(airspaceName, aircraftName, aircraft)

    // Kazuki doesn't support these actions
    override fun save(airspaceName: String) = UnsupportedAction
    override fun unload(airspaceName: String) = UnsupportedAction
    override fun load(airspaceName: String) = UnsupportedAction
    override fun setAirspace(airspaceName: String, opened: Boolean) = SetAirspaceAction(airspaceName, opened)
}

interface KazukiAction : Action {
    fun act(env: ExecutionEnvironment)
}
