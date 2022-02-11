package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.action

import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.action.PlayerMoveDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPos
import com.anaplan.engineering.azuki.vdm.DefaultVdmAction
import com.anaplan.engineering.azuki.vdm.EmptySystemContext
import com.anaplan.engineering.azuki.vdm.ModuleBuilder

class PlayerMoveAction(
    gameName: String,
    playerName: String,
    position: Position
) : PlayerMoveDeclarableAction(gameName, playerName, position), DefaultVdmAction
{
    override fun build(builder: ModuleBuilder<EmptySystemContext>): ModuleBuilder<EmptySystemContext> {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")
        val gameSetter = builder.setters[gameName] ?: throw IllegalStateException("Missing setter for game $gameName")

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.Pos.import,
                XOModule.move.import,
            ),
            testSteps = listOf("""
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl pos: ${XOModule.Pos} := ${toVdmPos(position)};
                    g := ${XOModule.move}(g, ${toVdmPlayer(playerName)}, pos);
                    ${gameSetter("g")};
                );
            """)
        )
    }
}
