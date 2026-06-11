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

data class Aircraft(val position: Position, val velocity: Velocity) {
    constructor(coordinates: Pair<Position, Velocity>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(position.toPair(), velocity.toPair())
    override fun toString(): String = "A($position, $velocity)"
}

typealias Aircrafts = Map<String, Aircraft>

const val delta_o = 100.0
const val delta_c = 1000.0
const val Theta_h = 80.0
const val MIN_AIRCRAFT = 5

enum class Quadrant { FRONT_RIGHT, FRONT_LEFT, BACK_LEFT, BACK_RIGHT }
enum class Crossing { Crossing, Crossed, ZeroCrossed, OneCrossed, BothCrossed }
enum class Convergence { Convergence, Divergence, Overtake }
enum class Direction { Same, Opposite }

fun Crossing.toRightOfWayBehaviours() = when(this) {
    Crossing.Crossing -> RightOfWayBehaviours.Crossing
    Crossing.Crossed -> RightOfWayBehaviours.Crossed
    Crossing.ZeroCrossed -> RightOfWayBehaviours.ZeroCrossed
    Crossing.OneCrossed -> RightOfWayBehaviours.OneCrossed
    Crossing.BothCrossed -> RightOfWayBehaviours.BothCrossed
}
