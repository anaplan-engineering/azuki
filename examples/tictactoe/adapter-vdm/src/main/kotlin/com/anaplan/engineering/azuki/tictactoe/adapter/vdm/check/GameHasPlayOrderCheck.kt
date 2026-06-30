package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check

import com.anaplan.engineering.azuki.tictactoe.adapter.api.GetPlayOrderBehaviour
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.toVdmSequence

class GameHasPlayOrderCheck(
    private val gameName: String,
    private val playOrder: List<String>
) : GetPlayOrderBehaviour(), DefaultVdmCheck {

    override fun build(builder: DefaultModuleBuilder): DefaultModuleBuilder {
        val gameGetter = builder.getters[gameName] ?: throw IllegalStateException("Missing getter for game $gameName")

        return builder.extend(
            requiredImports = setOf(
                XOModule.Game.import,
                XOModule.PlayOrder.import,
                XOModule.getPlayOrder.import,
            ),
            testSteps = listOf(
                """
                (
                    dcl g: ${XOModule.Game} := $gameGetter;
                    dcl expected: ${XOModule.PlayOrder} := ${toVdmSequence(playOrder.map(::toVdmPlayer))};
                    ${checkEquals(actual = "${XOModule.getPlayOrder}(g)")}
                );
                """
            )
        )
    }

}
