package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.DefaultVdmCheck

class GameIsDrawCheck(
    private val gameName: String
) : GetPlayOrderBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.isDraw.import,
            ),
            testSteps = listOf(
                """
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl expected: bool := true;
                    ${checkEquals(actual = "${XOModule.isDraw}(g)")}
                );
                """
            )
        )
    }

}
