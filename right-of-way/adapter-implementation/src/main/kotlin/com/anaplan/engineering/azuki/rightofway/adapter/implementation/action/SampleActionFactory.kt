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
    override fun start(airspaceName: String) = StartAirspaceAction(airspaceName)
    override fun save(airspaceName: String) = SaveAirspaceAction(airspaceName)
    override fun close(airspaceName: String) = CloseAirspaceAction(airspaceName)
    override fun load(airspaceName: String) = LoadAirspaceAction(airspaceName)
    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) =
        CreateAircraftAction(airspaceName, aircraftName, aircraft)
}

interface SampleAction : Action {
    fun act(env: ExecutionEnvironment)
}
