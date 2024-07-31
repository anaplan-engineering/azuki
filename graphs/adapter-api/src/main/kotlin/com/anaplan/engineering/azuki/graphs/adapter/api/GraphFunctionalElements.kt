package com.anaplan.engineering.azuki.graphs.adapter.api

import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphFunctionalElements.GraphFunction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IsA(
    val functionalElement: FunctionalElement
)

object GraphFunctionalElements {

    const val UndirectedGraph = 1
    const val DirectedGraph = 2
    const val GraphFunction = 3

}

object GraphFunctions {

    @IsA(GraphFunction)
    const val HasCycles = 4

    @IsA(GraphFunction)
    const val GetSimpleCycleCount = 5

    @IsA(GraphFunction)
    const val GetShortestPath = 6

}
