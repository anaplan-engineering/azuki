package com.anaplan.engineering.azuki.tictactoe.adapter.vdm

import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.vdm.toVdmMap
import com.anaplan.engineering.azuki.vdm.toVdmQuote

fun toVdmPlayer(player: String) =
    when (player) {
        "X" -> toVdmQuote("CROSS")
        "O" -> toVdmQuote("NOUGHT")
        else -> throw IllegalStateException("Invalid player name")
    }

fun toVdmPos(position: Position) = "mk_${XOModule.Pos}(${position.row}, ${position.col})"

fun toVdmMoves(moves: MoveMap) =
    toVdmMap(moves.map { (position, playerName) -> toVdmPos(position) to toVdmPlayer(playerName) }.toMap())
