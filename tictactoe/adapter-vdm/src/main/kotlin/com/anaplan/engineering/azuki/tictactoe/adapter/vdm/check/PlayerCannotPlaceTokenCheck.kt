package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlaceATokenBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPos
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder

class PlayerCannotPlaceTokenCheck(
    private val gameName: String,
    private val playerName: String,
    private val position: Position
) : PlaceATokenBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.Pos.import,
                XOModule.move.import,
            ),
            failingStep = """
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl pos: ${XOModule.Pos} := ${toVdmPos(position)};
                    let
                        g' = ${XOModule.move}(g, ${toVdmPlayer(playerName)}, pos)
                    in
                        return false
                );
            """
            )
    }
}
