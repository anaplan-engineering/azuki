package com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution

import com.anaplan.engineering.azuki.tictactoe.specification.dafny.Player.Player
import com.anaplan.engineering.azuki.tictactoe.specification.dafny.XO.__default
import dafny.DafnySequence

internal fun toDafnyPlayOrder(po: List<String>) =
    __default.createPlayOrder(DafnySequence.fromList(Player._typeDescriptor(), po.map { toDafnyPlayer(it) }))

internal fun toDafnyPlayer(p: String) =
    when (p) {
        "X" -> Player.create_Cross()
        "O" -> Player.create_Nought()
        else -> throw IllegalStateException("Invalid player name")
    }
