package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.AirspaceActionFactory
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.CreateAircraftDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.action.StartAirspaceDeclarableAction
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayRegardlessOf
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayWhen

object RightOfWayScriptGenerationActionFactory : RightOfWayActionFactory {

    override val airspace = AirspaceScriptGenerationActionFactory
}

fun interface RightOfWayScriptGenerationAction : ScriptGenerationAction<RightOfWayGenerationEnvironment> {

    override val behavior get() = unsupportedBehavior
}

object AirspaceScriptGenerationActionFactory : AirspaceActionFactory {

    override fun start(airspaceName: String) = StartAirspaceDeclarableAction(airspaceName, true)

    override fun save(airspaceName: String) = RightOfWayScriptGenerationAction {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayRegardlessOf::saveAirspace, airspaceName)
    }

    override fun close(airspaceName: String) = RightOfWayScriptGenerationAction {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayRegardlessOf::closeAirspace, airspaceName)
    }

    override fun load(airspaceName: String) = RightOfWayScriptGenerationAction {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayRegardlessOf::loadAirspace, airspaceName)
    }

    // addAircraft is complex as it can be used both in 'given' and 'when' positions
    // TODO LF: wasn't sure of difference here
//    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) = RightOfWayScriptGenerationAction {
//        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayRegardlessOf::addAircraft, airspaceName, aircraftName, aircraft)
//    }
    override fun addAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) =
        AddAircraft(airspaceName, aircraftName, aircraft)

    class AddAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) :
        CreateAircraftDeclarableAction(airspaceName, aircraftName, aircraft),
        ScriptGenerationAction<RightOfWayGenerationEnvironment> {

        override fun getActionScript(environment: RightOfWayGenerationEnvironment) =
            RightOfWayScriptingHelper.scriptifyFunction(RightOfWayWhen::placeAircraft, airspaceName, aircraftName, aircraft)
    }
}
