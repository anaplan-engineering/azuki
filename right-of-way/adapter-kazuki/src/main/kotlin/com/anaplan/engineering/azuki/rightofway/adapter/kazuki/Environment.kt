package com.anaplan.engineering.azuki.rightofway.adapter.kazuki

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft_Module
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace
import com.anaplan.engineering.azuki.rightofway.kazuki.Position_Module
import com.anaplan.engineering.azuki.rightofway.kazuki.Velocity_Module
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.kazuki.Airspace_Module.mk_Airspace
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.toNat

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

    internal fun aircraft(airspaceName: String, aircraftName: String) = get<Aircraft>("${airspaceName}_${aircraftName}")

    internal fun hasAircraft(airspaceName: String, aircraftName: String) =
        variables.containsKey("${airspaceName}_${aircraftName}")

    //TODO LF: remove? redundant...
    internal fun aircraftCount(airspaceName: String): ULong {
        require(variables.containsKey(airspaceName)) { "No such airspace $airspaceName" }
        return variables.keys.filter { it.startsWith("${airspaceName}_") }.size.toNat()
    }

    // because of the get method, this always returns non-null
    fun <T> withAirspace(name: String, op: Airspace.() -> T) = airspace(name).op()
}

//TODO change the overloaded names from across different projects - unhelpful

// Maps abstract `adapter-api` position to Kazuki's corresponding type
fun Position.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Position = Position_Module.mk_Position(x, y)
fun Velocity.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Velocity = Velocity_Module.mk_Velocity(x, y)
fun Aircraft.toKazuki(): com.anaplan.engineering.azuki.rightofway.kazuki.Aircraft = Aircraft_Module.mk_Aircraft(position.toKazuki(), velocity.toKazuki())
fun AirspaceDeclaration.toKazuki(): Airspace {
    require(aircrafts.values.toSet().size == aircrafts.values.size) { "Duplicate aircraft position found in: ${aircrafts.values}" }
    return mk_Airspace(as_Set(aircrafts.values.map { it.toKazuki() }.toSet()), delta_o, delta_c, Theta_h, opened)
}
