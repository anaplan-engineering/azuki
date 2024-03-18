package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.declarableaction

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.declaration.DeclarationStateFactory

class GameDeclarationState : DeclarationState() {

    object Factory: DeclarationStateFactory<GameDeclarationState> {
        override fun create() = GameDeclarationState()
    }
}




