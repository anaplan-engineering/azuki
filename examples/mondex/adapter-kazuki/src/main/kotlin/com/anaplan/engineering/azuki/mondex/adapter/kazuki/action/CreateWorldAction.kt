package com.anaplan.engineering.azuki.mondex.adapter.kazuki.action

import com.anaplan.engineering.azuki.mondex.adapter.api.AbPurse
import com.anaplan.engineering.azuki.mondex.adapter.declaration.action.CreateWorldDeclarableAction
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.ExecutionEnvironment
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.buildWorld

//LF @QST has to differentiate on what kind of world you want
class CreateWorldAction(
    authPurses: Map<String, Purse>,
    worldName: String = DEFAULT_WORLD,
    val beforeWorldName: String = BEFORE_WORLD
) : CreateWorldDeclarableAction(authPurses, worldName), KazukiAction {

    //LF @EK here we need a differentiator between what kind of purse/world to create
    override fun act(env: ExecutionEnvironment) {
        @Suppress("UNCHECKED_CAST")
        val abPurses = authPurses as Map<String, AbPurse>
        env.set(worldName, buildWorld(abPurses))
        env.set(beforeWorldName, buildWorld(abPurses))
    }

    companion object {
        private const val DEFAULT_WORLD = com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState.DEFAULT_WORLD
        private const val BEFORE_WORLD = com.anaplan.engineering.azuki.mondex.adapter.declaration.MondexDeclarationState.BEFORE_DEFAULT_WORLD
    }
}
