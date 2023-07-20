package com.anaplan.engineering.azuki.declaration

interface DeclarationStateFactory<S: DeclarationState> {
    fun create(): S
}
