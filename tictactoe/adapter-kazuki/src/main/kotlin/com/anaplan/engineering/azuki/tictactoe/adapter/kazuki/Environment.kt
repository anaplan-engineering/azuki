package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki

import com.anaplan.engineering.azuki.tictactoe.kazuki.XO

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

}

fun String.toPlayer(): XO.Player =
    when (this) {
        "X" -> XO.Player.Cross
        "O" -> XO.Player.Nought
        else -> throw IllegalArgumentException("Illegal player name: $this")
    }
