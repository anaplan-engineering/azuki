package com.anaplan.engineering.azuki.rightofway.adapter.api

data class Position(val x: Double, val y: Double) {
    //TODO Why doesn't inheritance works well with Data classes? equals/hash?
    //TODO should we really have the dubplication `data class Position` vs. `@Module interface Position`?
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(x, y)
    override fun toString(): String = "P($x, $y)"
}

// it !in positions? but this didn't work'
//operator fun Collection<Position>.contains(pair: Pair<Double, Double>): Boolean {
//    // Check if any Position matches the pair's values
//    return any { it.x == pair.first && it.y == pair.second }
//}

fun Pair<Double, Double>.toPosition(): Position = Position(this)

data class Velocity(val x: Double, val y: Double) {
    constructor(coordinates: Pair<Double, Double>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(x, y)
    override fun toString(): String = "V($x, $y)"
}

fun Pair<Double, Double>.toVelocity(): Velocity = Velocity(this)

data class Aircraft(val position: Position, val velocity: Velocity) {
    constructor(coordinates: Pair<Position, Velocity>) : this(coordinates.first, coordinates.second)

    fun toPair() = Pair(position.toPair(), velocity.toPair())
    override fun toString(): String = "A($position, $velocity)"
}

fun Pair<Pair<Double, Double>, Pair<Double, Double>>.toAircraft() =
    Aircraft(first.toPosition() to second.toVelocity())

typealias Aircrafts = Map<String, Aircraft>

const val DELTA_O = 100.0
const val DELTA_C = 1000.0
const val THETA_H = 80.0
const val MIN_AIRCRAFT = 5U

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

fun Boolean.toDirection() = if (this) Direction.Same else Direction.Opposite

fun freshNames(from: Set<String> = emptySet(), prefix: String = "a", start: Int = from.size) : Sequence<String> =
    generateSequence(prefix + start) { index ->
        val suffix = index.substringAfterLast(prefix).toIntOrNull() ?: start
        "$prefix${suffix + 1}"
    }

// maps adapter-api type from implementation type
fun freshPosition(positions: Set<Position>)  = Position(spiralPositionsSequence().first { it.toPosition() !in positions })
fun freshVelocity(velocities: Set<Velocity>) = Velocity(spiralVelocitiesSequence().first { it.toVelocity() !in velocities })

// Lazily creates fresh positions/velocities according to the airspace constants
fun spiralPositionsSequence(delta_c: Double = DELTA_C) = spiralSequence(delta_c)
fun spiralVelocitiesSequence(delta_o: Double = DELTA_O) = spiralSequence(delta_o)

// stepwise change of position/velocity in spiral pattern
private fun spiralSequence(step: Double): Sequence<Pair<Double, Double>> = sequence {
    var x = 0.0
    var y = 0.0
    var dx = 0.0
    var dy = step

    while (true) {
        yield(x to y)
        // Change direction when we hit a corner of the spiral boundary
        if (x == y || (x < 0.0 && x == -y) || (x > 0.0 && x == 1.0 - y)) {
            val temp = dx
            dx = -dy
            dy = temp
        }
        x += dx
        y += dy
    }
}
