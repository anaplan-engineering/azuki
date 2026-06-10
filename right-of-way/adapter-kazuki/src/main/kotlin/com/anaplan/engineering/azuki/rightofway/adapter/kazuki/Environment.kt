package com.anaplan.engineering.azuki.rightofway.adapter.kazuki

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft_Module
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace_Module
import com.anaplan.engineering.azuki.rightofway.kazuki.Position_Module
import com.anaplan.engineering.azuki.rightofway.kazuki.Velocity_Module
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.kazuki.core.as_Set
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class EnvironmentBuilder {

    private val declarations = LinkedHashMap<String, (ExecutionEnvironment) -> Any>()

    fun <T : Any> declare(name: String, declaration: (ExecutionEnvironment) -> T) {
        if (name in declarations.keys) {
            throw IllegalArgumentException("$name declared more than once")
        }
        declarations[name] = declaration
    }

    fun build(): ExecutionEnvironment {
        val env = ExecutionEnvironment()
        declarations.forEach { (n, fn) -> env.set(n, fn(env)) }
        return env
    }
}

class ExecutionEnvironment {

    val variables = mutableMapOf<String, Any>()

    inline fun <reified T> get(name: String) = variables[name] as T

    inline fun <reified T : Any> set(name: String, value: T) {
        variables[name] = value
    }

    internal fun airspace(name: String) = get<Airspace>(name)
}

//TODO change the overloaded names from across different projects - unhelpful

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
private val objectMapper = jacksonObjectMapper()

fun String.toAirspace(): Airspace {
    val coordinates: Map<String, List<List<Double>>> = objectMapper.readValue(trimIndent())
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
    //TODO LF: Do we need to check the Kazuki invariants? Guess not, but creating a airspace will be involved given theorems
    return aircrafts.toKazukiAirspace()
}

// Maps abstract `adapter-api` position to Kazuki's corresponding type
fun Position.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Position = Position_Module.mk_Position(x, y)
fun Velocity.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Velocity = Velocity_Module.mk_Velocity(x, y)
fun Aircraft.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft = Aircraft_Module.mk_Aircraft(position.toKazuki(), velocity.toKazuki())
fun Aircrafts.toKazukiAirspace(): Airspace {
    require(values.toSet().size == values.size) { "Duplicate aircraft position found in: $values" }
    return Airspace_Module.mk_Airspace(as_Set(values.map { it.toKazuki() }.toSet()))
}
