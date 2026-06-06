package com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts

data class AirspaceDeclaration(
    override val name: String,
    val aircrafts: Aircrafts = emptyMap(),
    override val standalone: Boolean = true,
) : Declaration
