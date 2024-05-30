package com.anaplan.engineering.azuki.graphs.analysis.directed

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

class SimpleCycleCount : GraphScenario() {

    @AnalysisScenario
    fun graphHasACycle() {
        given {
            thereIsADirectedGraph(graphA) {
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
    fun graphHasNoCycles() {
        given {
            thereIsADirectedGraph(graphA) {
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
    fun graphHasMultipleCycles() {
        given {
            thereIsADirectedGraph(graphA) {
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

    @AnalysisScenario
    fun graphWithMultipleLargerCycles() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("c", "e")
                edge("a", "f")
                edge("f", "g")
                edge("h", "a")
                edge("i", "h")
                edge("g", "i")
            }
        }
        then {
            hasSimpleCycleCount(graphA, 2)
        }
    }

    @AnalysisScenario
    fun graphWithMultipleUnconnectedCycles() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("c", "e")
                edge("j", "f")
                edge("f", "g")
                edge("h", "j")
                edge("i", "h")
                edge("g", "i")
            }
        }
        then {
            hasSimpleCycleCount(graphA, 2)
        }
    }

    @AnalysisScenario
    fun graphWithInverselyDeclaredEdgesDirected() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("c", "e")

                edge("b", "a")
                edge("c", "b")
                edge("a", "d")
                edge("d", "e")
                edge("e", "c")
            }
        }
        then {
            hasSimpleCycleCount(graphA, 7)
        }
    }
}

