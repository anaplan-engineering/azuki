package com.anaplan.engineering.azuki.rightofway.adapter.implementation.action

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class SampleActionFactory : RightOfWayActionFactory {
    override val airspace = SampleAirspaceActionFactory
}

object SampleAirspaceActionFactory : AirspaceActionFactory {
    override fun start(airspaceName: String, delta_o: Double, delta_c: Double, theta_h: Double, opened: Boolean) =
        StartAirspaceAction(airspaceName, delta_o, delta_c, theta_h, opened)
    override fun save(airspaceName: String) = SaveAirspaceAction(airspaceName)
    override fun unload(airspaceName: String) = UnloadAirspaceAction(airspaceName)
    override fun load(airspaceName: String) = LoadAirspaceAction(airspaceName)
    override fun setAirspace(airspaceName: String, opened: Boolean) = SetAirspaceAction(airspaceName, opened)
    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) =
        CreateAircraftAction(airspaceName, aircraftName, aircraft)
}

interface SampleAction : Action {
    fun act(env: ExecutionEnvironment)
}
