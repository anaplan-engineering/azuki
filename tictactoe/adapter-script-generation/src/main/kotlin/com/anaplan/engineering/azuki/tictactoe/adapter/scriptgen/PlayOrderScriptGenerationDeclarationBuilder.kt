package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGiven

class PlayOrderScriptGenerationDeclarationBuilder(declaration: PlayOrderDeclaration) :
    ScriptGenerationDeclarationBuilder<TicTacToeGenerationEnvironment, PlayOrderDeclaration>(declaration) {

    override fun getDeclarationScript(environment: TicTacToeGenerationEnvironment) =
        TicTacToeScriptingHelper.scriptifyFunction(
            TicTacToeGiven::thereIsAPlayOrder,
            declaration.name,
            declaration.playOrder,
        )

    class Factory : TicTacToeScriptGenerationDeclarationBuilderFactory<PlayOrderDeclaration> {

        override val declarationClass = PlayOrderDeclaration::class.java

        override fun create(declaration: PlayOrderDeclaration) =
            PlayOrderScriptGenerationDeclarationBuilder(declaration)
    }
}
