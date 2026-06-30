package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class WithVertices : GraphScenario() {

    @AnalysisScenario
    fun graphWithVertices() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                vertex("a")
                vertex("b")
            }
        }
        then {
            hasVertexCount(graphA, 2)
        }
    }

    @AnalysisScenario
    fun directedGraphWithVertices() {
        given {
            thereIsADirectedGraph(graphA) {
                vertex("a")
                vertex("b")
            }
        }
        then {
            hasVertexCount(graphA, 2)
        }
    }
}
