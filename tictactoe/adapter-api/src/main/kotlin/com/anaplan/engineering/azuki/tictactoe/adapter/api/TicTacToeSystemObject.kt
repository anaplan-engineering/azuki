package com.anaplan.engineering.azuki.tictactoe.adapter.api

data class Position(val row: Int, val col: Int) {
    constructor(coordinates: Pair<Int, Int>) : this(coordinates.first, coordinates.second)
}

typealias MoveMap = Map<Position, String>

fun MoveMap.pretty(rowMax:Int, colMax: Int): String = buildString {
    (1..rowMax).forEach { row ->
        (1 until colMax).forEach { col ->
            append(getOrDefault(Position(row, col), "."))
            append(" | ")
        }
        append("${getOrDefault(Position(row, colMax), ".")}\n")
    }
}
