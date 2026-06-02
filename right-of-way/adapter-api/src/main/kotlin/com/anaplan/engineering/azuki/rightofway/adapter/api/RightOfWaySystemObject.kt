package com.anaplan.engineering.azuki.rightofway.adapter.api

enum class Quadrant { FRONT_RIGHT, FRONT_LEFT, BACK_LEFT, BACK_RIGHT }

data class Position(val x: Double, val y: Double) {
    //TODO Why doesn't inheritance works well with Data classes? equals/hash?
    //TODO should we really have the dubplication `data class Position` vs. `@Module interface Position`?
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)
}

data class Velocity(val x: Double, val y: Double) {
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)
}

data class Aircraft(val p: Position, val v: Velocity) {
    constructor(coordinates: Pair<Position, Velocity>) : this(coordinates.first, coordinates.second)
}

typealias PositionMap = Map<Position, String>
typealias VelocityMap = Map<Velocity, String>
typealias AircraftMap = Map<Aircraft, String>

fun PositionMap.pretty(rowMax:Int, colMax: Int): String = buildString {
    (1..rowMax).forEach { x ->
        (1 until colMax).forEach { y ->
            append(getOrDefault(Position(x.toDouble(), y.toDouble()), "."))
            append(" | ")
        }
        append(getOrDefault(Position(x.toDouble(), colMax.toDouble()), "."))
        appendLine()
    }
}
