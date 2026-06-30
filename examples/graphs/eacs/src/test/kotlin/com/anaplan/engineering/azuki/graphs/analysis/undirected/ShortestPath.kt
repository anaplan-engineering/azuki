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
            hasShortestPath(graphA, "a", "b", "c")
            hasShortestPath(graphA, "e", "c")
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
            hasShortestPath(graphA, "e", "d", "a", "b", "c")
        }
    }


    @AnalysisScenario
    fun equalPathLengths() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("c", "d")

                edge("a", "e")
                edge("e", "f")
                edge("f", "d")
            }
            thereIsAnUndirectedGraph("graphB") {
                edge("a", "e")
                edge("e", "f")
                edge("f", "d")

                edge("a", "b")
                edge("b", "c")
                edge("c", "d")
            }
        }
        then {
            hasShortestPath(graphA, "a", "b", "c", "d")
            hasShortestPath("graphB", "a", "e", "f", "d")
        }
    }

    @AnalysisScenario
    fun noPath() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                vertex("a")
            }
        }
        then {
            hasShortestPath(graphA, "a")
        }
    }
}
