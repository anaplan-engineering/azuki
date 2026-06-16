package com.anaplan.engineering.azuki.rightofway

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity

const val airspaceUK = "UK"
const val airspaceUS = "US"

const val aircraft0 = "a0"
const val aircraft1 = "a1"
const val aircraft2 = "a2"

val a0a1Airspace = """
    {
        "a0": [[-3.0, 3.0], [2.0, 1.0]],
        "a1": [[3.0, 4.0], [1.0, 2.0]]
    }
""".trimIndent()

val Aircraft0 = Aircraft(Position(-3.0, 3.0), Velocity(2.0, 1.0))
val Aircraft1 = Aircraft(Position(3.0, 4.0), Velocity(1.0, 2.0))
