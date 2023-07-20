package com.anaplan.engineering.azuki.declaration

class DeclarationStateBuilder<S : DeclarationState>(private val factory: DeclarationStateFactory<S>) {

    fun build(declarationActions: List<DeclarableAction<S>>): List<Declaration> {
        val declarationState = factory.create()
        declarationActions.forEach { it.declare(declarationState) }
        return declarationState.getDeclarations().filter { it.value.standalone }.map { it.value }
    }

}
