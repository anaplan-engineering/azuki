package com.anaplan.engineering.azuki.tictactoe.dsl.ascii

import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position

object TicTacToeBoardAscii {

    fun parse(boardData: String): MoveMap {
        val table = boardData.lines().filterNot(String::isBlank).map { board ->
            board.split('|')
                .map(String::trim)
                .map { if (it == ".") null else it }
        }
        return table.flatMapIndexed { i, row ->
            // Cannot filter before mapping, otherwise we'll lose the position information
            row.mapIndexed { j, playerName ->
                if (playerName == null) {
                    null
                } else {
                    Position(i + 1, j + 1) to playerName
                }
            }
        }.filterNotNull().toMap()
    }
}
