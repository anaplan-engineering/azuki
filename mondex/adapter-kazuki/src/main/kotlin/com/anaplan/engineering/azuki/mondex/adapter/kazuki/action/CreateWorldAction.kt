package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.declaration.action.CreateWorldDeclarableAction
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.buildWorld

class CreateWorldAction(
    authPurses: Map<String, Purse>,
    worldName: String = DEFAULT_WORLD,
) : CreateWorldDeclarableAction(authPurses, worldName), KazukiAction {

    override fun act(env: ExecutionEnvironment) {
        env.set(worldName, buildWorld(authPurses))
    }

    companion object {
        private const val DEFAULT_WORLD = com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState.DEFAULT_WORLD
    }
}
