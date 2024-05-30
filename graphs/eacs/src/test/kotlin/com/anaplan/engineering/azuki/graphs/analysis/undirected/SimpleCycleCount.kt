package com.anaplan.engineering.azuki.graphs.analysis.undirected

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class SimpleCycleCount : GraphScenario() {

    @AnalysisScenario
    fun graphWithCycle() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("c", "e")
            }
        }
        then {
            hasSimpleCycleCount(graphA, 1)
        }
    }

    @AnalysisScenario
    fun graphWithNoCycles() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("c", "d")
            }
        }
        then {
            hasSimpleCycleCount(graphA, 0)
        }
    }

    @AnalysisScenario
    fun graphWithMultipleCycles() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("c", "e")
                edge("b", "f")
                edge("f", "a")
            }
        }
        then {
            hasSimpleCycleCount(graphA, 2)
        }
    }
}

