package com.anaplan.engineering.azuki.rightofway.adapter.implementation.actionGenerator

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.api.freshNames
import com.anaplan.engineering.azuki.rightofway.adapter.api.spiralPositionsSequence
import com.anaplan.engineering.azuki.rightofway.adapter.api.spiralVelocitiesSequence
import com.anaplan.engineering.azuki.rightofway.adapter.api.toAircraft
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.rightofway.implementation.Airspace
import kotlin.sequences.forEach

class SampleActionGeneratorFactory : RightOfWayActionGeneratorFactory {

    override fun generateAirspace(airspaceName: String) = SampleActionGenerator { env ->
        require(airspaceName !in env.airspaceManager.activeAirspaces) { "Airspace name $airspaceName already generated or declared" }

        listOf { af -> af.airspace.start(airspaceName) }
    }

    override fun generateAircraft(airspaceName: String, numberOfAircraft: UInt) = SampleActionGenerator { env ->
        env.withAirspace(airspaceName) {
            require(0U < numberOfAircraft) { "Number of moves must be strictly-positive (> 0)" }

            // get as many fresh names from known aircraft names as requested and return an action factory call list
            freshNames().zip(spiralPositionsSequence().zip(spiralVelocitiesSequence())).map {
                // zipped result is Sequence<String, Pair<Position, Velocity>>
                    (name, zipped) -> name to zipped.toAircraft() }.take(numberOfAircraft.toInt()).map {
                    (aircraftName, aircraft) ->
                        // result is a list of lambda from RightOfWayActionFactory() -> Action (!)
                { af: RightOfWayActionFactory ->
                    // TODO LF: These positions are not guaranteed to be safe! Filter? Map in Kazuki?
                    af.airspace.addAircraft(airspaceName, aircraftName, aircraft)
                }
            }.toList()
        }
    }
}

fun interface SampleActionGenerator : ActionGenerator {

    fun generate(env: ExecutionEnvironment): List<(RightOfWayActionFactory) -> Action>
}


//// maps adapter-api type from implementation type
//fun Airspace.freshPosition() = Position(spiralPositionsSequence().first { it !in positions })
//fun Airspace.freshVelocity() = Velocity(spiralVelocitiesSequence().first { it !in velocities })
//
//// Lazily creates fresh positions/velocities according to the airspace constants
//fun Airspace.spiralPositionsSequence() = spiralSequence(delta_c)
//fun Airspace.spiralVelocitiesSequence() = spiralSequence(delta_o)
//
//// stepwise change of position/velocity in spiral pattern
//fun spiralSequence(step: Double): Sequence<Pair<Double, Double>> = sequence {
//    var x = 0.0
//    var y = 0.0
//    var dx = 0.0
//    var dy = step
//
//    while (true) {
//        yield(x to y)
//        // Change direction when we hit a corner of the spiral boundary
//        if (x == y || (x < 0.0 && x == -y) || (x > 0.0 && x == 1.0 - y)) {
//            val temp = dx
//            dx = -dy
//            dy = temp
//        }
//        x += dx
//        y += dy
//    }
//}
