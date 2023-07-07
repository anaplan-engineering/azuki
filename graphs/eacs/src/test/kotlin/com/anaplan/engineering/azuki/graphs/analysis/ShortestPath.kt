package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class ShortestPath: GraphScenario() {

    @AnalysisScenario
    fun graphWithEdges() {
        given {
            thereIsAGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("a", "d")
                edge("d", "e")
                edge("e", "c")
            }
        }
        then {
            hasShortestPath(graphA, "a", "c", "b")
        }
    }
}
