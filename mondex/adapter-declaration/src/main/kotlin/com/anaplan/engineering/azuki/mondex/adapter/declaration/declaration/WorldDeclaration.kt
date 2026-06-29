package com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse

data class WorldDeclaration(
    override val name: String,
    val authPurses: Map<String, Purse>,
    override val standalone: Boolean,
) : Declaration
