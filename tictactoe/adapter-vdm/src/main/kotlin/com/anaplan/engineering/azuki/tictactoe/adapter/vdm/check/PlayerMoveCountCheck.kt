package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayerMoveCountBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.DefaultVdmCheck

class PlayerMoveCountCheck(
    private val gameName: String,
    private val player: String,
    private val times: Int
) : PlayerMoveCountBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.movesForPlayer.import,
            ),
            testSteps = listOf(
                """
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl expected: nat := ${times};
                    ${checkEquals(actual = "card ${XOModule.movesForPlayer}(g, ${toVdmPlayer(player)})")}
                );
                """
            )
        )
    }

}
