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

    @Eac("when first initialised graph has no edges or vertices")
    fun newGraph() {
        given {
            thereIsADirectedGraph(graphA)
        }
        then {
            hasVertexCount(graphA, 0)
        }
    }

    @Eac("A graph may contain any number of vertices")
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

    @Eac("graph multiple edges")
    fun withEdges() {
        given {
            thereIsADirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("d", "e")
            }
        }
        then {
            hasVertexCount(graphA, 5)
        }
    }

    @Eac("A directed graph is a graph that only contains directed edges",
        "A directed edge is an edge with ordered endpoints")
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
}
