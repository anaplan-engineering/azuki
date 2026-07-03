package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.PurseDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.TransferDeclaration
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration.WorldDeclaration
import kotlin.collections.set

class IntraWorldDeclarationState : DeclarationState() {

    fun declareWorld(worldName: String) {
        checkForDuplicate(worldName)
        declarations[worldName] = WorldDeclaration(worldName, emptyList(), emptyList(), standalone = true)
    }

    fun declarePurse(purseName: String, balance: Int) {
        checkForDuplicate(purseName)
        declarations[purseName] = PurseDeclaration(purseName, balance, 0, standalone = true)
    }

    fun declareTransfer(transferName: String, fromPurse: String, toPurse: String, amount: Int) {
        checkForDuplicate(transferName)
        declarations[transferName] = TransferDeclaration(transferName, fromPurse, toPurse, amount, standalone = true)
    }

    fun addPurseToWorld(worldName: String, purseName: String) {
        checkExists(worldName)
        checkExists(purseName)
        val declaration = getDeclaration<WorldDeclaration>(worldName)
        declarations[worldName] = declaration.copy(
            purses = declaration.purses + purseName
        )
    }

    fun addTransferToWorld(worldName: String, transferName: String) {
        checkExists(worldName)
        checkExists(transferName)
        val declaration = getDeclaration<WorldDeclaration>(worldName)
        declarations[worldName] = declaration.copy(
            transfers = declaration.transfers + transferName
        )
    }

}
