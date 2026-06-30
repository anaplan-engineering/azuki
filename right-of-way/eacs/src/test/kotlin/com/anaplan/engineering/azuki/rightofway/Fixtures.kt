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

/** Converging pair: QC true, HMD ~1.414, going_to_cross both ways, zero_crossed true. */
val convergingAirspace = """
    {
        "a0": [[3.0, 9.0], [2.0, 1.0]],
        "a1": [[5.0, 5.0], [1.0, 2.0]]
    }
""".trimIndent()

/** Same geometry as convergingAirspace but opposite a1 velocity: QC true, track delta 180°, headon true. */
val headOnAirspace = """
    {
        "a0": [[3.0, 9.0], [2.0, 1.0]],
        "a1": [[5.0, 5.0], [-2.0, -1.0]]
    }
""".trimIndent()

/** a0a1 pair: one_crossed true (a1 going to cross a0, a0 already crossed a1), crossed(a0,a1) true. */
val oneCrossedAirspace = a0a1Airspace

val position0 = Position(3.0, 3.0)
val position1 = Position(30.0, 40.0)
val velocity0 = Velocity(2.0, 1.0)
val velocity1 = Velocity(1.0, 2.0)
