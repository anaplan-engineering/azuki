package com.anaplan.engineering.azuki.mondex.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.mondex.adapter.api.TransferDetails
import com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration.WorldDeclaration
import com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration.WorldOperation

class MondexDeclarationState : DeclarationState() {

    fun declareWorld(worldName: String, authPurses: Map<String, Pair<ULong, ULong>>) {
        checkForDuplicate(worldName)
        declarations[worldName] = WorldDeclaration(worldName, authPurses, emptyList(), standalone = true)
    }

    companion object {
        const val DEFAULT_WORLD = "world"
    }
}
