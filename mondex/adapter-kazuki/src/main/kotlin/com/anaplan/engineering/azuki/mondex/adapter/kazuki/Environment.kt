package com.anaplan.engineering.azuki.mondex.adapter.kazuki

import com.anaplan.engineering.azuki.mondex.kazuki.World
import com.anaplan.engineering.azuki.mondex.kazuki.Purse_Module.mk_Purse
import com.anaplan.engineering.azuki.mondex.kazuki.World_Module.mk_World
import com.anaplan.engineering.azuki.mondex.adapter.api.Purse
import com.anaplan.engineering.kazuki.core.*
import kotlin.collections.component1
import kotlin.collections.component2

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

    internal fun world(name: String) = get<World>(name)
}

fun buildWorld(
    authPurses: Map<String, Purse>,
): World {
    val world = mk_World(
        mapping(authPurses.entries) { (name, purse) ->
            mk_(name, mk_Purse(purse.balance, purse.lost))
        },
    )
    return world
}

fun World.withPurse(
    purseName: String,
    purse: Purse,
) : World {
    val world = mk_World(
        authPurses
            * mk_Mapping(mk_(purseName,  mk_Purse(purse.balance, purse.lost)))
    )
    return world
}

fun Map<String, Purse>.toMapping() =
    mapping(this.entries) { (name, purse) ->
        mk_(name, mk_Purse(purse.balance, purse.lost)) }

//fun Purse.toKazukiPurse(): KazukiPurse = when (this) {
//    is AbPurse -> mk_Purse(balance, lost)
//    is ConPurse -> mk_Purse(balance, 0uL) // or whatever the Con→Ab projection is
//}
