package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.NoEnvScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.NoScriptGenerationEnvironment
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGiven

class PlayOrderScriptGenerationDeclarationBuilder(declaration: PlayOrderDeclaration) :
    NoEnvScriptGenerationDeclarationBuilder<PlayOrderDeclaration>(declaration) {

    override fun getDeclarationScript(): String =
        TicTacToeScriptingHelper.scriptifyFunction(
            TicTacToeGiven::thereIsAPlayOrder,
                declaration.name,
                declaration.playOrder,
            )

    class Factory : ScriptGenerationDeclarationBuilderFactory<NoScriptGenerationEnvironment, PlayOrderDeclaration> {

        override val declarationClass = PlayOrderDeclaration::class.java

        override fun create(declaration: PlayOrderDeclaration) = PlayOrderScriptGenerationDeclarationBuilder(declaration)
    }
}
