package com.anaplan.engineering.azuki.rightofway.adapter.api

data class Position(val x: Double, val y: Double) {
    //TODO Why doesn't inheritance works well with Data classes? equals/hash?
    //TODO should we really have the dubplication `data class Position` vs. `@Module interface Position`?
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(x, y)
    override fun toString(): String = "P($x, $y)"
}

data class Velocity(val x: Double, val y: Double) {
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(x, y)
    override fun toString(): String = "V($x, $y)"
}

data class Aircraft(val p: Position, val v: Velocity) {
    constructor(coordinates: Pair<Position, Velocity>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(p.toPair(), v.toPair())
    override fun toString(): String = "A($p, $v)"
}

typealias Aircrafts = MutableMap<String, Aircraft>

const val delta_o = 100.0
const val delta_c = 1000.0
const val Theta_h = 80.0

enum class Quadrant { FRONT_RIGHT, FRONT_LEFT, BACK_LEFT, BACK_RIGHT }

//typealias PositionMap = Map<Position, String>
//typealias VelocityMap = Map<Velocity, String>
//typealias AircraftMap = Map<Aircraft, String>


//fun PositionMap.pretty(rowMax:Int, colMax: Int): String = buildString {
//    (1..rowMax).forEach { x ->
//        (1 until colMax).forEach { y ->
//            append(getOrDefault(Position(x.toDouble(), y.toDouble()), "."))
//            append(" | ")
//        }
//        append(getOrDefault(Position(x.toDouble(), colMax.toDouble()), "."))
//        appendLine()
//    }
//}
