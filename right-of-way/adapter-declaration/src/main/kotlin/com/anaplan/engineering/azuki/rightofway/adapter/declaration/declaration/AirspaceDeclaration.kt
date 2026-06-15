package com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts

data class AirspaceDeclaration(
    override val name: String,
    val aircrafts: Aircrafts = emptyMap(),
    val delta_o: Double,
    val delta_c: Double,
    val Theta_h: Double,
    val opened: Boolean,// = false,
    override val standalone: Boolean = true,
) : Declaration
