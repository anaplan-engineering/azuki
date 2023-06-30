package com.anaplan.engineering.azuki.graphs.adapter.declaration

import com.anaplan.engineeering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.graphs.adapter.declaration.declaration.GraphDeclaration

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

    fun declareGraph(graphName: String) {
        checkForDuplicate(graphName)
        declarations[graphName] = GraphDeclaration<Any>(graphName, standalone = true)
    }

}

class DuplicateDeclarationException(def: String) : IllegalArgumentException("$def is already defined")
class MissingDeclarationException(def: String) : IllegalArgumentException("$def is not defined")
