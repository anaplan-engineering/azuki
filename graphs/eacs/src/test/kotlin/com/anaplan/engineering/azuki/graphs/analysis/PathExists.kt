package com.anaplan.engineering.azuki.graphs.analysis

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class PathExists : GraphScenario() {

    @AnalysisScenario
    fun noPath() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                vertex('a')
                vertex('b')
            }
        }
        then {
            noPathExists(graphA, from = 'a', to = 'b')
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
            pathExists(graphA, from = 'a', to = 'b')
            pathExists(graphA, from = 'b', to = 'a')
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
            pathExists(graphA, from = 'a', to = 'b')
            noPathExists(graphA, from = 'b', to = 'a')
        }
    }
}
