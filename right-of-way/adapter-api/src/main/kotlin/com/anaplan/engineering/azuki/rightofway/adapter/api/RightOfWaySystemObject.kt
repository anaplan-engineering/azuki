package com.anaplan.engineering.azuki.rightofway.adapter.api

data class Position(val x: Double, val y: Double) {
    //TODO Why doesn't inheritance works well with Data classes? equals/hash?
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)
}

data class Velocity(val x: Double, val y: Double) {
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)
}

typealias MoveMap = Map<Position, String>
typealias VelocityMap = Map<Velocity, String>

fun MoveMap.pretty(rowMax:Int, colMax: Int): String = buildString {
    (1..rowMax).forEach { x ->
        (1 until colMax).forEach { y ->
            append(getOrDefault(Position(x.toDouble(), y.toDouble()), "."))
            append(" | ")
        }
        append(getOrDefault(Position(x.toDouble(), colMax.toDouble()), "."))
        appendLine()
    }
}
