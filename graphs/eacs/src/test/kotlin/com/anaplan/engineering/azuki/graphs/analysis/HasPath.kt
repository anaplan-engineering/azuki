package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class HasPath : GraphScenario() {

    @AnalysisScenario
    fun noPath() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                vertex('a')
                vertex('b')
            }
        }
        then {
            hasPath(graphA, 'a', 'b', false)
        }
    }

    @AnalysisScenario
    fun undirectedPath() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge('a', 'b')
            }
        }
        then {
            hasPath(graphA, 'a', 'b', true)
            hasPath(graphA, 'b', 'a', true)
        }
    }

    @AnalysisScenario
    fun directedGraph() {
        given {
            thereIsADirectedGraph(graphA) {
                edge('a', 'b')
            }
        }
        then {
            hasPath(graphA, 'a', 'b', true)
            hasPath(graphA, 'b', 'a', false)
        }
    }
}
