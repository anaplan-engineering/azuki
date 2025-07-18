package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilder
import com.anaplan.engineering.azuki.script.generation.ScriptGenDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeGiven

class GameScriptGenDeclarationBuilder(declaration: GameDeclaration) :
    ScriptGenDeclarationBuilder<GameDeclaration>(declaration) {
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
                declaration.moves.toAscii(),
            )
        }

    class Factory : ScriptGenDeclarationBuilderFactory<GameDeclaration> {

        override val declarationClass = GameDeclaration::class.java

        override fun create(declaration: GameDeclaration): ScriptGenDeclarationBuilder<GameDeclaration> =
            GameScriptGenDeclarationBuilder(declaration)
    }
}
