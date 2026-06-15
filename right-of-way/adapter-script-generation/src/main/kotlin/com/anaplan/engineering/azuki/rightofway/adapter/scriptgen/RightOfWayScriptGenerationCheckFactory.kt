package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

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

    override val behavior get() = unsupportedBehavior
}

object AirspaceScriptGenerationCheckFactory : AirspaceCheckFactory {

    override fun hasPlayOrder(gameName: String, players: List<String>) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::gameHasPlayOrder, gameName, players)
    }

    override fun hasToken(gameName: String, playerName: String, position: Position) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::boardHasToken, gameName, playerName, position)
    }.asComposable {
        airspaceCheckStates.tryRegister(gameName) { hasToken(playerName, position) }
    }

    override fun hasSpace(gameName: String, position: Position) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::boardHasSpace, gameName, position)
    }.asComposable {
        airspaceCheckStates.tryRegister(gameName) { hasSpace(position) }
    }

    override fun hasState(gameName: String, moves: MoveMap) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::boardHasState, gameName,
            // add a newline to avoid """ and board being on same line
            "\n" + moves.pretty(3, 3))
    }

    override fun isComplete(gameName: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::boardIsComplete, gameName)
    }

    override fun isDraw(gameName: String) = RightOfWayScriptGenerationCheck {
        RightOfWayScriptingHelper.scriptifyFunction(RightOfWayThen::gameIsDraw, gameName)
    }
}
