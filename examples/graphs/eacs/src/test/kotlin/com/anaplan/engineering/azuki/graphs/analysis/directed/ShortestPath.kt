package com.anaplan.engineering.azuki.graphs.analysis.directed

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class ShortestPath : GraphScenario() {

    @AnalysisScenario
    fun directedGraph() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("c", "d")
                edge("a", "e")
                edge("d", "e")
            }
        }
        then {
            hasShortestPath(graphA, "a", "b", "c", "d")
        }
    }
}
