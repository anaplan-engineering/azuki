package com.anaplan.engineering.azuki.rightofway.adapter.implementation.actionGenerator

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace

class SampleActionGeneratorFactory : RightOfWayActionGeneratorFactory {

    override fun generateAirspace(airspaceName: String) = SampleActionGenerator { env ->
        require(airspaceName !in env.airspaceManager.activeAirspaces) { "Airspace name $airspaceName already generated or declared" }

        listOf { af -> af.airspace.start(airspaceName) }
    }

    override fun generateAircraft(airspaceName: String, numberOfAircraft: Int) = SampleActionGenerator { env ->
        env.withAirspace(airspaceName) {
            require(0 < numberOfAircraft) { "Number of moves must be strictly-positive (> 0)" }

            // get as many fresh names from known aircraft names as requested and return an action factory call list
            freshNames(aircraftIds).take(numberOfAircraft).map { aircraftName ->
                { af: RightOfWayActionFactory ->
                    // TODO LF: These positions are not guaranteed to be safe! Filter? Map in Kazuki?
                    af.airspace.addAircraft(airspaceName, aircraftName, freshPosition(), freshVelocity()) }
            }.toList()
        }
    }
}

fun interface SampleActionGenerator : ActionGenerator {

    fun generate(env: ExecutionEnvironment): List<(RightOfWayActionFactory) -> Action>
}

fun freshNames(from: Set<String> = emptySet(), prefix: String = "a", start: Int = from.size) : Sequence<String> =
    generateSequence(prefix + start) { index ->
        val suffix = index.substringAfterLast(prefix).toIntOrNull() ?: start
        "$prefix${suffix + 1}"
    }

// maps adapter-api type from implementation type
fun Airspace.freshPosition() = Position(spiralPositionsSequence().first { it !in positions })
fun Airspace.freshVelocity() = Velocity(spiralVelocitiesSequence().first { it !in velocities })

// Lazily creates fresh positions/velocities according to the airspace constants
fun Airspace.spiralPositionsSequence() = spiralSequence(delta_c)
fun Airspace.spiralVelocitiesSequence() = spiralSequence(delta_o)

// stepwise change of position/velocity in spiral pattern
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
