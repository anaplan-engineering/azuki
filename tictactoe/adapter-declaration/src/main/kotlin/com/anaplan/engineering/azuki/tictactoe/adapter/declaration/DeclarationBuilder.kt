package com.anaplan.engineering.azuki.tictactoe.adapter.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration

class DeclarationBuilder(private val declarationActions: List<DeclarableAction>) {

    private val declarations = LinkedHashMap<String, Declaration>()

    private inline fun <reified T : Declaration> getDeclaration(name: String): T =
        declarations[name] as T? ?: throw MissingDeclarationException(name)

    fun build(): List<Declaration> {
        declarationActions.forEach { it.declare(this) }
        return declarations.filter { it.value.standalone }.map { it.value }
    }

    private fun checkForDuplicate(name: String) {
        if (declarations.containsKey(name)) throw DuplicateDeclarationException(name)
    }

    private fun checkExists(name: String) {
        if (!declarations.containsKey(name)) throw MissingDeclarationException(name)
    }

    fun declarePlayOrder(orderName: String, playOrder: List<String>, standalone: Boolean = true) {
        checkForDuplicate(orderName)
        declarations[orderName] = PlayOrderDeclaration(orderName, playOrder, standalone)
    }

    fun declareGame(boardName: String, orderName: String) {
        checkForDuplicate(boardName)
        checkExists(orderName)
        declarations[boardName] = GameDeclaration(boardName, orderName, emptyMap(),true)
    }

    fun playerMove(gameName: String, playerName: String, position: Position) {
        val game = getDeclaration<GameDeclaration>(gameName)
        declarations[gameName] = game.copy(moves = game.moves.plus(position to playerName))
    }

}

class DuplicateDeclarationException(def: String) : IllegalArgumentException("$def is already defined")
class MissingDeclarationException(def: String) : IllegalArgumentException("$def is not defined")
