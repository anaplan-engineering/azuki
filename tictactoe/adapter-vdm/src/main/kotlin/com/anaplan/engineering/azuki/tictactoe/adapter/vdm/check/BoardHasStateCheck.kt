package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmMoves
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.DefaultVdmCheck
import com.anaplan.engineering.azuki.vdm.toVdmSequence

class BoardHasStateCheck(
    private val gameName: String,
    private val moves: MoveMap
) : GetPlayOrderBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

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
                    ${checkEquals(actual = "${XOModule.getBoard}(g)")}
                );
                """
            )
        )
    }

}
