package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl.json

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.api.toPosition
import com.anaplan.engineering.azuki.rightofway.adapter.api.toVelocity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object RightOfWayAirspaceJSON {

    private val objectMapper = jacksonObjectMapper()

    /**
     * Expect a JSON-like string representing air spaces. For now, keep it simple as
     *
     * val jsonString1 = """
     *     {
     *       "Alpha": [[1.0, 2.0], [3.0, 4.0] ],
     *       "Beta":  [[1.0, 2.0], [7.0, 8.0] ],
     *       "Beta": [[1.0, 2.0], [7.0, 10.0] ]
     *     }
     * """.trimIndent() // name repetition overrides; position repetition fails
     *
     * Alternatively, we could have it with explicit position / velocity records, but would require serialization of data classes
     *
     * val jsonString2 = """
     *     {
     *       "Alpha": {
     *          "position": { "x": 1.0, "y": 2.0 },
     *          "velocity": { "x": 3.0, "y": 4.0 }
     *       },
     *       "Beta": {
     *          "position": { "x": 5.0, "y": 6.0 },
     *          "velocity": { "x": 7.0, "y": 8.0 }
     *       }
     *     }
     * """.trimIndent()
     */
    fun parse(airspaceData: String): Aircrafts {
        val coordinates: Map<String, List<List<Double>>> = objectMapper.readValue(airspaceData.trimIndent())
        val aircrafts: Aircrafts = coordinates.mapValues { (_, coords) ->
            require(coords.size == 2) { "Expected [position, velocity] for aircraft coordinates: $coords" }
            val (position, velocity) = coords
            require(position.size == 2 && velocity.size == 2) {
                "Expected [x, y] pairs for position and velocity: $coords"
            }
            val (xp, yp) = position
            val (xv, yv) = velocity
            Aircraft(
                Position(xp, yp),
                Velocity(xv, yv),
            )
        }
        return aircrafts
    }
}


