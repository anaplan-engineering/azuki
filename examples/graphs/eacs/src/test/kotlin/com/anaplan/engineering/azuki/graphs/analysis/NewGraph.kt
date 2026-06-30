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

    fun directedWithEdges() {
        given {
            thereIsADirectedGraph(graphA) {
                edge('a', 'b')
                edge('b', 'c')
                edge('c', 'a')
                edge('d', 'e')
            }
        }
        then {
            hasVertexCount(graphA, 5)
        }
    }
}
