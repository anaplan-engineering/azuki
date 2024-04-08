package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class NewGraph : GraphScenario() {

    @AnalysisScenario
    fun newGraphIsEmpty() {
        given {
            thereIsAnUndirectedGraph(graphA)
        }
        then {
            hasVertexCount(graphA, 0)
        }
    }

    @AnalysisScenario
    fun newDirectedGraphIsEmpty() {
        given {
            thereIsADirectedGraph(graphA)
        }
        then {
            hasVertexCount(graphA, 0)
        }
    }
}
