package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.NoEnvScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.NoScriptGenerationEnvironment
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.pretty
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGiven

class GameScriptGenerationDeclarationBuilder(declaration: GameDeclaration) :
    NoEnvScriptGenerationDeclarationBuilder<GameDeclaration>(declaration) {
    private val newGameOrder: kotlin.reflect.KFunction3<TicTacToeGiven, String, String, Unit> = TicTacToeGiven::thereIsANewGame
    private val gameOrder: kotlin.reflect.KFunction4<TicTacToeGiven, String, String, String, Unit> = TicTacToeGiven::thereIsAGame

    override fun getDeclarationScript(): String =
        if (declaration.moves.isEmpty()) {
            TicTacToeScriptingHelper.scriptifyFunction(
                newGameOrder,
                declaration.name,
                declaration.orderName,
            )
        } else {
            TicTacToeScriptingHelper.scriptifyFunction(
                gameOrder,
                declaration.name,
                declaration.orderName,
                declaration.moves.pretty(3, 3),
            )
        }

    class Factory : ScriptGenerationDeclarationBuilderFactory<NoScriptGenerationEnvironment, GameDeclaration> {

        override val declarationClass = GameDeclaration::class.java

        override fun create(declaration: GameDeclaration): NoEnvScriptGenerationDeclarationBuilder<GameDeclaration> =
            GameScriptGenerationDeclarationBuilder(declaration)
    }
}
