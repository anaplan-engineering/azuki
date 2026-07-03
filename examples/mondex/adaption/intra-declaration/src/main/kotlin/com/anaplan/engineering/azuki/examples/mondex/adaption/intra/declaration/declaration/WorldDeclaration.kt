package com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration

data class WorldDeclaration(
    override val name: String,
    val purses: List<String>,
    val transfers: List<String>,
    override val standalone: Boolean,
) : Declaration
