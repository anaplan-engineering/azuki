package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationCheck
import com.anaplan.engineering.azuki.script.generation.asComposable
import com.anaplan.engineering.azuki.rightofway.adapter.api.*
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayThen

object RightOfWayScriptGenerationCheckFactory : RightOfWayCheckFactory {

    override val airspace = AirspaceScriptGenerationCheckFactory

    override fun systemValid() = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::everythingIsOkay)
    }
}

fun interface RightOfWayScriptGenerationCheck : ScriptGenerationCheck<RightOfWayGenerationEnvironment> {

    // TODO LF: should this be each of the behaviours expected?
    override val behavior get() = unsupportedBehavior
}

object AirspaceScriptGenerationCheckFactory : AirspaceCheckFactory {

    override fun hasAircraft(airspaceName: String, aircraftName: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasAircraft, airspaceName, aircraftName)
    }

    override fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasRightOfWay, airspaceName, aircraft0, aircraft1 )
    }

    override fun isConverging(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::isConverging, airspaceName, aircraft0, aircraft1 )
    }

    override fun isOvertaking(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::isOvertaking, airspaceName, aircraft0, aircraft1 )
    }

    override fun isHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::isHeadOn, airspaceName, aircraft0, aircraft1 )
    }

    override fun isNotHeadOn(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::isNotHeadOn, airspaceName, aircraft0, aircraft1 )
    }

    override fun isGoingToCross(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::isGoingToCross, airspaceName, aircraft0, aircraft1 )
    }

    override fun hasCrossed(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasCrossed, airspaceName, aircraft0, aircraft1 )
    }

    override fun hasZeroCrossed(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasZeroCrossed, airspaceName, aircraft0, aircraft1 )
    }

    override fun hasOneCrossed(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasOneCrossed, airspaceName, aircraft0, aircraft1 )
    }

    override fun hasBothCrossed(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasBothCrossed, airspaceName, aircraft0, aircraft1 )
    }

    override fun hasQuadrantConvergence(airspaceName: String, aircraft0: String, aircraft1: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasQuadrantConvergence, airspaceName, aircraft0, aircraft1 )
    }

    override fun horizontalMissDistanceIs(airspaceName: String, aircraft0: String, aircraft1: String, hmd: Double) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::horizontalMissDistanceIs, airspaceName, aircraft0, aircraft1, hmd )
    }

    override fun hasOrientation(airspaceName: String, aircraft0: String, aircraft1: String, same: Boolean) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::hasOrientation, airspaceName, aircraft0, aircraft1, same )
    }

    override fun timeToClosestPointApproachIs(airspaceName: String, aircraft0: String, aircraft1: String, tcpa: Double) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::timeToClosestPointApproachIs, airspaceName, aircraft0, aircraft1, tcpa )
    }

    override fun onTrack(airspaceName: String, aircraftName: String, angle: Double) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::onTrack, airspaceName, aircraftName, angle )
    }

    override fun onQuadrantRelativeTo(airspaceName: String, aircraft0: String, aircraft1: String, quadrant: Quadrant) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::onQuadrantRelativeTo, airspaceName, aircraft0, aircraft1, quadrant )
    }
}
