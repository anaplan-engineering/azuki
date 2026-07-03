package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

data class PurseDeclaration(
    override val name: String,
    val balance: Int,
    val lost: Int,
    override val standalone: Boolean
): Declaration
