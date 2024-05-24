package com.anaplan.engineering.azuki.graphs.analysis.undirected

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA


class ShortestPath : GraphScenario() {

    @AnalysisScenario
    fun graphWithEdges() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("a", "d")
                edge("d", "e")
                edge("e", "c")
            }
        }
        then {
            hasShortestPath(graphA, from = "a", to = "c", path = arrayOf("a", "b", "c"))
            hasShortestPath(graphA, from = "e", to = "c", path = arrayOf("e", "c"))
        }
    }

    @AnalysisScenario
    fun graphWithLongerPath() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("a", "d")
                edge("d", "e")
            }
        }
        then {
            hasShortestPath(graphA, from = "e", to = "c", path = arrayOf("e", "d", "a", "b", "c"))
        }
    }

    @AnalysisScenario
    fun test() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                vertex("d")
            }
        }
        then {
            hasShortestPath(graphA, from = "a", to = "d")
        }
    }
}
