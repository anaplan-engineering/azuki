package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmMoves
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPos
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder

class BoardHasTokenCheck(
    private val gameName: String,
    private val player: String,
    private val position: Position,
) : GetPlayOrderBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        val moves = mapOf(position to player)

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.Pos.import,
                XOModule.Player.import,
                XOModule.getBoard.import,
            ),
            testSteps = listOf(
                """
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl expected: map ${XOModule.Pos} to ${XOModule.Player} := ${toVdmMoves(moves)};
                    ${checkEquals(actual = "{${toVdmPos(position)}} <: ${XOModule.getBoard}(g)")}
                );
                """
            )
        )
    }

}
