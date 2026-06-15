package com.anaplan.engineering.azuki.rightofway.implementation

import org.slf4j.Logger

// Avoid explicit dependencies with adapter-api on purpose
typealias Position = Pair<Double, Double>
typealias Velocity = Pair<Double, Double>
typealias Aircraft = Pair<Position, Velocity>
typealias AircraftData = Map<String, Aircraft>
typealias Aircrafts = MutableMap<String, Aircraft>

fun Aircraft.position(): Position = first
fun Aircraft.velocity(): Velocity = second
fun Pair<Double, Double>.x(): Double = first
fun Pair<Double, Double>.y(): Double = second

data class AirspaceState(
    val delta_o: Double,
    val delta_c: Double,
    val Theta_h: Double,
    val open: Boolean,
    val aircrafts: Aircrafts = mutableMapOf()
)

abstract class Airspace protected constructor(
    val state: AirspaceState):
    QuadrantBehaviours ,
    PositionBehaviours ,
    OrientationBehaviours ,
    CrossingBehaviours,
    ConvergenceBehaviours,
    RightOfWayBehaviours
{
    protected abstract val log: Logger

    // shadow the data class
    private val aircrafts by state::aircrafts
    val delta_o by state::delta_o
    val delta_c by state::delta_c
    val Theta_h by state::Theta_h
    val open by state::open

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

    fun getAircraft(id: String) = aircrafts[id] ?: throw IllegalArgumentException("No such aircraft $id")

    fun hasAircraft(id: String) = aircrafts.containsKey(id)

    fun addAircraft(id: String, position: Position, velocity: Velocity) {
        require(!aircrafts.containsKey(id)) { "Aircraft $id already exists" }
        require(!positions.contains(position)) { "Position $position already exists for aircraft $id" }
        aircrafts[id] = position to velocity
    }

}
