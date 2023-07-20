package com.anaplan.engineering.azuki.declaration

import java.util.*
import kotlin.collections.LinkedHashMap

abstract class DeclarationState {

    protected val declarations = LinkedHashMap<String, Declaration>()

    fun getDeclarations() = Collections.unmodifiableMap(declarations)

    protected inline fun <reified T : Declaration> getDeclaration(name: String): T =
        declarations[name] as T? ?: throw MissingDeclarationException(name)

    protected fun checkForDuplicate(name: String) {
        if (declarations.containsKey(name)) throw DuplicateDeclarationException(name)
    }

    protected fun checkExists(name: String) {
        if (!declarations.containsKey(name)) throw MissingDeclarationException(name)
    }

    class DuplicateDeclarationException(def: String) : IllegalArgumentException("$def is already defined")
    class MissingDeclarationException(def: String) : IllegalArgumentException("$def is not defined")
}


