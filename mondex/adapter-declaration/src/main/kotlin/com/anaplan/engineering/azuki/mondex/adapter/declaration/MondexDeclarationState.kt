package com.anaplan.engineering.azuki.mondex.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration.WorldDeclaration

class MondexDeclarationState : DeclarationState() {

    fun declareWorld(worldName: String, authPurses: Map<String, Purse>) {
        checkForDuplicate(worldName)
        declarations[worldName] = WorldDeclaration(worldName, authPurses, standalone = true)
    }

    companion object {
        const val DEFAULT_WORLD = "world"
    }
}
