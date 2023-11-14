package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki

import com.anaplan.engineering.azuki.tictactoe.kazuki.XO

class AnimationBuilder {

    internal val declarations = mutableListOf<XO.() -> Unit>()
    internal val commands = mutableListOf<XO.() -> Unit>()
    internal val checks = mutableListOf<XO.() -> Boolean>()

    fun declare(declaration: XO.() -> Unit) = declarations.add(declaration)
    fun act(command: XO.() -> Unit) = commands.add(command)
    fun check(check: XO.() -> Boolean) = checks.add(check)
}
