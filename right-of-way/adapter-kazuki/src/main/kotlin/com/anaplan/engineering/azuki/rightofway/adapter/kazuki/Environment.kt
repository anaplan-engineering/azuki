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

    inline fun <reified T> get(name: String) = (variables[name] as T) ?: throw IllegalArgumentException("No such name $name")

    inline fun <reified T : Any> set(name: String, value: T) {
        variables[name] = value
    }

    internal fun airspace(name: String) = get<Airspace>(name)

    internal fun aircraft(airspaceName: String, aircraftName: String) = get<Aircraft>("${airspaceName}.${aircraftName}")

    internal fun hasAircraft(airspaceName: String, aircraftName: String) =
        variables.containsKey("${airspaceName}.${aircraftName}")

    // because of the get method, this always returns non-null
    fun <T> withAirspace(name: String, op: Airspace.() -> T) = airspace(name).op()
}

//TODO change the overloaded names from across different projects - unhelpful

// Maps abstract `adapter-api` position to Kazuki's corresponding type
fun Position.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Position = Position_Module.mk_Position(x, y)
fun Velocity.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Velocity = Velocity_Module.mk_Velocity(x, y)
fun Aircraft.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft = Aircraft_Module.mk_Aircraft(position.toKazuki(), velocity.toKazuki())
fun Aircrafts.toKazukiAirspace(): Airspace {
    require(values.toSet().size == values.size) { "Duplicate aircraft position found in: $values" }
    return Airspace_Module.mk_Airspace(as_Set(values.map { it.toKazuki() }.toSet()))
}
