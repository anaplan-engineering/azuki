package com.anaplan.engineering.azuki.graphs.adapter.declaration

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

}

class DuplicateDeclarationException(def: String) : IllegalArgumentException("$def is already defined")
class MissingDeclarationException(def: String) : IllegalArgumentException("$def is not defined")
