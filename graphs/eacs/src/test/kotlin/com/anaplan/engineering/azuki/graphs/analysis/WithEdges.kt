package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class WithEdges: GraphScenario() {

    @AnalysisScenario
    fun graphWithEdges() {
        given {
            thereIsAGraph(graphA) {
                edge("a", "b")
            }
        }
        then {
            hasVertexCount(graphA, 2)
        }
    }
}
