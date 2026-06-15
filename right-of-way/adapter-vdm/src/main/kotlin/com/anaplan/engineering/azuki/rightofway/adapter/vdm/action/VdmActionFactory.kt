package com.anaplan.engineering.azuki.rightofway.adapter.vdm.action

import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory

class VdmActionFactory : RightOfWayActionFactory {
    override val airspace = VdmAirspaceActionFactory
}

object VdmAirspaceActionFactory : AirspaceActionFactory {
    override fun start(airspaceName: String) = StartAirspaceAction(airspaceName, true)
    override fun save(airspaceName: String) = UnsupportedAction
    override fun close(airspaceName: String) = StartAirspaceAction(airspaceName, false)
    override fun load(airspaceName: String) = UnsupportedAction
    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) =
        CreateAircraftAction(airspaceName, aircraftName, aircraft)
}
