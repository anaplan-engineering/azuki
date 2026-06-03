package com.anaplan.engineering.azuki.rightofway.adapter.implementation.actionGenerator

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment
import kotlin.sequences.zip

class SampleActionGeneratorFactory : RightOfWayActionGeneratorFactory {

    override fun generateAirspace(airspaceName: String) = SampleActionGenerator { env ->
        require(airspaceName !in env.airspaceManager.activeAirspaces) { "Airspace name $airspaceName already generated or declared" }

        listOf { af -> af.airspace.start(airspaceName) }
    }

    override fun generateAircraft(airspaceName: String, numberOfAircraft: Int) = SampleActionGenerator { env ->
        env.withAirspace(airspaceName) {
            require(0 < numberOfAircraft) { "Number of moves must be strictly-positive (> 0)" }

            // get as many fresh names from known aircraft names as requested and return an action factory call list
            freshNames(aircraftIds).take(numberOfAircraft).map { name ->
                { af: RightOfWayActionFactory -> af.airspace.addAircraft(airspaceName, name) }
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

//
//val nameSet = setOf("New Document", "New Document (1)")
//val baseName = "New Document"
//
//// Generates: "New Document", "New Document (1)", "New Document (2)"...
//val freshName = generateSequence(baseName) { index ->
//    val suffix = index.substringAfterLast("(").substringBeforeLast(")").toIntOrNull() ?: 0
//    "$baseName (${suffix + 1})"
//}.first { it !in nameSet } // Result: "New Document (2)"

// TODO should this be here instead of Implementation?
///**
// * Lazily generates an infinite stream of (X, Y) coordinates winding outward
// * as a spiral from (0, 0).
// */
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
