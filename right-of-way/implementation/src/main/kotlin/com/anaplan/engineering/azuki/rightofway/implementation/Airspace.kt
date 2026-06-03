package com.anaplan.engineering.azuki.rightofway.implementation

import org.slf4j.Logger

// Avoid explicit dependencies with adapter-api on purpose
typealias Position = Pair<Double, Double>
typealias Velocity = Pair<Double, Double>
typealias Aircraft = Pair<Position, Velocity>
typealias AircraftData = Map<String, Aircraft>
typealias Aircrafts = MutableMap<String, Aircraft>

data class AirspaceState(
    val delta_o: Double,
    val delta_c: Double,
    val Theta_h: Double,
    val aircrafts: Aircrafts = mutableMapOf()
)

abstract class Airspace protected constructor(
    val state: AirspaceState
) {
    protected abstract val log: Logger

    // shadow the data class
    private val aircrafts by state::aircrafts
    val delta_o by state::delta_o
    val delta_c by state::delta_c
    val Theta_h by state::Theta_h

    val size by lazy { aircrafts.size }
    val aircraftIds by lazy { aircrafts.keys }
    val positions by lazy { aircrafts.map { it.value.first }.toSet() }
    val velocities by lazy { aircrafts.map { it.value.second }.toSet() }

    override fun toString() =
        aircrafts.entries.joinToString(
            separator = "\n",
            prefix = "----- Airspace -----\n",
            postfix = "\n--------------------"
        ) { (key, value) -> "$key -> $value" }

//    fun getAircraft(id: String): Aircraft? {
//        require(aircrafts.containsKey(id)) { "No such aircraft $id" }
//        aircrafts[id] //?: throw IllegalArgumentException("No such aircraft $id")
//    }
    fun getAircraft(id: String) = aircrafts[id] ?: throw IllegalArgumentException("No such aircraft $id")

    fun getAircraftAt(position: Position) = aircrafts.values.find { it.first == position }

    fun getPosition(id: String) = getAircraft(id).first
    fun getVelocity(id: String) = getAircraft(id).second

    fun hasAircraft(id: String) = aircrafts.containsKey(id)
    fun hasPosition(position: Position) = positions.contains(position)

    fun addAircraft(id: String, position: Position, velocity: Velocity) {
        require(!aircrafts.containsKey(id)) { "Aircraft $id already exists" }
        require(!positions.contains(position)) { "Position $position already exists for aircraft $id" }
        aircrafts[id] = position to velocity
    }

    fun addAircraft(id: String) {
        addAircraft(id, freshPosition(), freshVelocity())
    }
}

// Lazily creates fresh positions/velocities according to the airspace constants
fun Airspace.spiralPositionsSequence() = spiralSequence(delta_c)
fun Airspace.spiralVelocitiesSequence() = spiralSequence(delta_o)

fun Airspace.freshPosition() = spiralPositionsSequence().first { it !in positions }
fun Airspace.freshVelocity() = spiralVelocitiesSequence().first { it !in velocities }

/**
 * Lazily generates an infinite stream of (X, Y) coordinates winding outward
 * as a spiral from (0, 0).
 */
fun spiralSequence(step: Double): Sequence<Pair<Double, Double>> = sequence {
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
