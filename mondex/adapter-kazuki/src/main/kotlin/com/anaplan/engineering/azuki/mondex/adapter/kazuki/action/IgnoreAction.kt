package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.api.IgnoreBehaviour
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.kazuki.abstractNullInput

class IgnoreAction(
    val worldName: String = DEFAULT_WORLD
) : IgnoreBehaviour(), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        env.set(worldName, env.world(worldName).functions.abstractIgnore(abstractNullInput))
    }

    companion object {
        private const val DEFAULT_WORLD = com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState.DEFAULT_WORLD
    }
}
