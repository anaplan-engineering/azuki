package com.anaplan.engineeering.azuki.declaration

import java.util.*

/**
 * DeclarationBuilder factory for a single functional element
 */
interface FeDeclarationBuilderFactory<D : Declaration, DB : DeclarationBuilder<D>> {
    val declarationClass: Class<D>
    fun create(declaration: D): DB
}

/**
 * DeclarationBuilder factory for all declarations in an implementation
 */
class DeclarationBuilderFactory<DBF : FeDeclarationBuilderFactory<*, *>>(
    private val builderClass: Class<DBF>
) {
    private val builderFactories by lazy {
        val loader = ServiceLoader.load(builderClass)
        loader.iterator().asSequence().map {
            it.declarationClass to it
        }.toMap()
    }

    fun <D : Declaration, DB : DeclarationBuilder<D>> createBuilder(declaration: D): DB {
        val factory = builderFactories[declaration.javaClass]
            ?: throw IllegalArgumentException("No declaration builder found for $declaration")
        @Suppress("UNCHECKED_CAST")
        return (factory as FeDeclarationBuilderFactory<D, DB>).create(declaration)
    }
}
