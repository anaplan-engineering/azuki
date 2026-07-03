package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

data class TransferDeclaration(
    override val name: String,
    val from: String,
    val to: String,
    val amount: Int,
    override val standalone: Boolean
) : Declaration
