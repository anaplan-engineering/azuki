package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder

class PlayerHasLostCheck(
    private val gameName: String,
    private val playerName: String,
) : GetPlayOrderBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.Player.import,
                XOModule.hasLost.import,
            ),
            testSteps = listOf(
                """
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl p: ${XOModule.Player} := ${toVdmPlayer(playerName)};
                    dcl expected: bool := true;
                    ${checkEquals(actual = "${XOModule.hasLost}(g, p)")}
                );
                """
            )
        )
    }

}
