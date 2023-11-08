package com.anaplan.engineering.azuki.tictactoe.adapter.implementation

import com.anaplan.engineering.azuki.tictactoe.implementation.Game
import com.anaplan.engineering.azuki.tictactoe.implementation.GameManager
import com.anaplan.engineering.azuki.tictactoe.implementation.Player
import com.anaplan.engineering.azuki.tictactoe.implementation.Token

class ExecutionEnvironment(val gameManager: GameManager) {

    val playOrders = mutableMapOf<String, List<String>>()

    fun <T> withGame(name: String, op: Game.() -> T) = gameManager[name].op()

}

internal fun toPlayer(symbol: String) =
    when (symbol) {
        "X" -> Player("Cross", Token.Cross)
        "O" -> Player("Circle", Token.Circle)
        else -> TODO("Unrecognised player $symbol")
    }
