package com.anaplan.engineering.azuki.graphs.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphBehaviours
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphFunctions
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

@BEH(GraphBehaviours.GetSimpleCycleCount, GraphFunctions.GetSimpleCycleCount, """
    Determine the number of simple cycles in a given graph
""")
class BEH7 : GraphScenario() {

    @Eac("A simple cycle is a path which starts and ends at the same vertex")
    fun hasTwoWayEdgesDirected() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("c", "d")
                edge("d", "e")
                edge("e", "c")
                edge("c", "a")
            }
        }
        then {
            getSimpleCycleCount(graphA, 2)
        }
    }

    @Eac("When there are no simple cycles in the given graph, return 0")
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
            getSimpleCycleCount(graphA, 0)
        }
    }

    @Eac("""
        When the given graph is directed, any path which would be considered a simple cycle in an undirected graph,
        but where two edges start (or end) at the same vertex is not counted as a simple cycle
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
            getSimpleCycleCount(graphA, 0)
        }
    }

    @Eac("When the given graph is empty, return 0")
    fun hasEmptyDirectedGraph() {
        given {
            thereIsADirectedGraph(graphA) {}
        }
        then {
            getSimpleCycleCount(graphA, 0)
        }
    }

    @Eac("Multiple simple cycles can still be counted separately if they share one or more edges")
    fun hasSubCircleDirected() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "a")
                edge("e", "d")
                edge("c", "f")
                edge("f", "e")
                edge("b", "e")
            }
        }
        then {
            getSimpleCycleCount(graphA, 2)
        }
    }

    @Eac("A simple cycle can consist of a pair of vertices with two edges between them",
        "If the given graph is directed, the two edges must have opposite directions.")
    fun hasEdgesInBothDirections() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "a")
            }
        }
        then {
            getSimpleCycleCount(graphA, 1)
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
            getSimpleCycleCount(graphA, 1)
        }
    }
}
