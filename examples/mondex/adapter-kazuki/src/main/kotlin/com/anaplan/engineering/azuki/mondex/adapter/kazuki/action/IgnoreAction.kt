package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.api.IgnoreBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.kazuki.abs.aNullIn

class IgnoreAction(
    val worldName: String = DEFAULT_WORLD
) : IgnoreBehaviour(), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        env.set(worldName, env.world(worldName).functions.abIgnore(aNullIn))
    }

    companion object {
        private const val DEFAULT_WORLD = com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState.DEFAULT_WORLD
    }
}
