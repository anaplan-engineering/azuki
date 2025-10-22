package com.anaplan.engineering.azuki.declaration

fun interface DeclarationStateFactory<S: DeclarationState> {

    fun create(): S
}
