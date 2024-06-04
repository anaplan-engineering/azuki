package com.anaplan.engineering.azuki.graphs.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphBehaviours
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphFunctionalElements
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA


@BEH(GraphBehaviours.CreateDirectedGraph, GraphFunctionalElements.DirectedGraph, """
    Create a directed graph
""")
class BEH8 : GraphScenario() {

    @Eac("A directed graph is a graph that only contains directional edges",
        """When a directional edge is declared a path can only be walked in one order
        """)
    fun directionalEdge() {
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

    @Eac("A graph may any positive number of vertices")
    fun withVertices() {
        given {
            thereIsADirectedGraph(graphA) {
                vertex("a")
                vertex("b")
                vertex("c")
            }
        }
        then {
            hasVertexCount(graphA, 3)
        }
    }

    @Eac("A graph may contain no vertices")
    fun noVertices() {
        given {
            thereIsADirectedGraph(graphA)
        }
        then {
            hasVertexCount(graphA, 0)
        }
    }

    @Eac("A directed graph can contain any number of edges",
        "These edges can be connected or independent of other edges"
    )
    fun withEdges() {
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

    @Eac("Multiple directed graphs can exist and do not interfere with each other")
    fun multipleGraphs() {
        given {
            thereIsADirectedGraph(graphA) {
                vertex('a')
                edge('b', 'c')
            }
            thereIsADirectedGraph("graphB") {
                vertex('d')
                edge('e', 'f')
                vertex('g')
            }
        }
        then {
            hasVertexCount(graphA, 3)
            hasVertexCount("graphB", 4)
        }
    }
}
