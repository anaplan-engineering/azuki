package com.anaplan.engineering.azuki.tictactoe.adapter.api

data class Position(val row: Int, val col: Int) {
    constructor(coordinates: Pair<Int, Int>) : this(coordinates.first, coordinates.second)
}

typealias MoveMap = Map<Position, String>
