package com.anaplan.engineering.azuki.mondex.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration.WorldDeclaration

class MondexDeclarationState : DeclarationState() {

    fun declareWorld(worldName: String, authPurses: Map<String, Purse>) {
        checkForDuplicate(worldName)
        declarations[worldName] = WorldDeclaration(worldName, authPurses, standalone = true)
    }

    fun declarePurse(purseName: String, purse: Purse) {
        checkExists(DEFAULT_WORLD)
        val world = getDeclaration<WorldDeclaration>(DEFAULT_WORLD)
        declarations[DEFAULT_WORLD] = world.copy(
            authPurses = world.authPurses + (purseName to purse)
        )
    }

    companion object {
        const val DEFAULT_WORLD = "world"
    }
}
