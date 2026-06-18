package com.anaplan.engineering.azuki.rightofway

import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity

const val airspaceUK = "UK"
const val airspaceUS = "US"

const val a0 = "a0"
const val a1 = "a1"
const val a2 = "a2"

val a0a1Airspace = """
    {
        "a0": [[3.0, 3.0], [2.0, 1.0]],
        "a1": [[30.0, 40.0], [1.0, 2.0]]
    }
""".trimIndent()

val position0 = Position(3.0, 3.0)
val position1 = Position(30.0, 40.0)
val velocity0 = Velocity(2.0, 1.0)
val velocity1 = Velocity(1.0, 2.0)
