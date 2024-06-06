package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class WithEdges : GraphScenario() {

    @AnalysisScenario
    fun graphWithEdges() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
            }
        }
        then {
            hasEdgeCount(graphA, 1)

        }
    }

    @AnalysisScenario
    fun directedGraphWithEdges() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
            }
        }
        then {
            hasEdgeCount(graphA, 1)
        }
    }
}
