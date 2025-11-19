package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPos
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder

class PlayerCanPlaceTokenCheck(
    private val gameName: String, private val playerName: String, private val position: Position, private val expected: Boolean,
) : PlaceATokenBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        return builder.extend(requiredImports = setOf(
            XOModule.Game.import,
            XOModule.Player.import,
            XOModule.Pos.import,
            XOModule.isValidMove.import,
        ), testSteps = listOf("""
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl p: ${XOModule.Player} := ${toVdmPlayer(playerName)};
                    dcl pos: ${XOModule.Pos} := ${toVdmPos(position)};
                    dcl expected: bool := $expected;
                    ${checkEquals(actual = "${XOModule.isValidMove}(g, p, pos)")}
                );
            """))
    }
}
