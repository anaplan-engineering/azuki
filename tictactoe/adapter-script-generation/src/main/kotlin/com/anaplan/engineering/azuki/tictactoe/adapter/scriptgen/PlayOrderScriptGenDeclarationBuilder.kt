package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGiven

class PlayOrderScriptGenDeclarationBuilder(declaration: PlayOrderDeclaration) :
    ScriptGenDeclarationBuilder<PlayOrderDeclaration>(declaration) {

    override fun getDeclarationScript(): String =
        TicTacToeScriptingHelper.scriptifyFunction(
            TicTacToeGiven::thereIsAPlayOrder,
                declaration.name,
                declaration.playOrder,
            )

    class Factory : ScriptGenDeclarationBuilderFactory<PlayOrderDeclaration> {

        override val declarationClass = PlayOrderDeclaration::class.java

        override fun create(declaration: PlayOrderDeclaration): ScriptGenDeclarationBuilder<PlayOrderDeclaration> =
            PlayOrderScriptGenDeclarationBuilder(declaration)
    }
}
