package com.anaplan.engineering.azuki.declaration

import com.anaplan.engineering.azuki.core.system.Action

interface DeclarableAction<S : DeclarationState> : Action {
    fun declare(state: S)
}

@Suppress("UNCHECKED_CAST")
fun <S : DeclarationState> toDeclarableAction(action: Action): DeclarableAction<S> =
    action as? DeclarableAction<S> ?: throw IllegalArgumentException("Invalid action: $action")
