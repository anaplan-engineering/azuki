package com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.rightofway.adapter.api.AircraftMap

class AirspaceDeclaration(
    override val name: String,
    val aircrafts: AircraftMap = emptyMap(),
    override val standalone: Boolean = true,
) : Declaration
