package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class NewGraph: GraphScenario() {

    @AnalysisScenario
    fun newGraphIsEmpty() {
        given {
            thereIsAGraph(graphA)
        }
        then {
            hasVertexCount(graphA, 0)
        }
    }
}
