package com.anaplan.engineering.azuki.graphs.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphBehaviours
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphFunctions
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

@BEH(GraphBehaviours.HasCycles, GraphFunctions.HasCycles, """
    Determine if a given graph contains one or more cycles
""")
class BEH6 : GraphScenario() {

    @Eac("A cycle is a walk which starts and ends at the same vertex")
    fun hasOneCycleDirected() {
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
            hasCycles(graphA, true)
        }
    }

    @Eac("When there are no cycles in the given graph, return false")
    fun hasNoCyclesDirected() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
            }
        }
        then {
            hasCycles(graphA, false)
        }
    }

    @Eac("""
        When the given graph is directed, any walk which would be considered a cycle in an undirected graph but
        where two edges start (or end) at the same vertex is not counted as a cycle
    """)
    fun hasReversedEdgeDirected() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("e", "c")
            }
        }
        then {
            hasCycles(graphA, false)
        }
    }

    @Eac("When the given graph is empty, return false")
    fun hasEmptyDirectedGraph() {
        given {
            thereIsADirectedGraph(graphA) {}
        }
        then {
            hasCycles(graphA, false)
        }
    }

    @Eac("A cycle can consist of a pair of vertices with two edges between them",
        "If the given graph is directed, the two edges must have opposite directions.")
    fun hasEdgesInBothDirections() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "a")
            }
        }
        then {
            hasCycles(graphA, true)
        }
    }


    @Eac("A cycle can consist of a single edge which starts and ends at the same vertex")
    fun hasLoopDirected() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "a")
            }
        }
        then {
            hasCycles(graphA, true)
        }
    }
}
