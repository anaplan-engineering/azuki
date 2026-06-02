package com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.rightofway.adapter.api.PositionMap
import com.anaplan.engineering.azuki.rightofway.adapter.api.VelocityMap

class AircraftDeclaration(
    override val name: String,
//    val moves: PositionMap = emptyMap(),
//    val speeds: VelocityMap = emptyMap(),
    override val standalone: Boolean = true,
) : Declaration
