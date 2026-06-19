package com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

data class WorldDeclaration(
    override val name: String,
    val authPurses: Map<String, Pair<ULong, ULong>>,
    val operations: List<WorldOperation>,
    override val standalone: Boolean,
) : Declaration

sealed interface WorldOperation {
    data class Transfer(val from: String, val to: String, val value: ULong) : WorldOperation
    data object Ignore : WorldOperation
    data class AddPersonWithPurse(val name: String, val balance: ULong, val lost: ULong) : WorldOperation
}
