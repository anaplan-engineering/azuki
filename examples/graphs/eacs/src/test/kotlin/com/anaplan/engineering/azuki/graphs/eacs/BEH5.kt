package com.anaplan.engineering.azuki.graphs.eacs

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphBehaviours
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphFunctions
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario
import com.anaplan.engineering.azuki.graphs.graphA

@BEH(GraphBehaviours.GetShortestPath, GraphFunctions.GetShortestPath, """
    Determine the shortest path between given start and end vertices in a given graph
""")
class BEH5 : GraphScenario() {

    @Eac("""
        If there is at least one path between the start and end vertex in the given graph, then a shortest path exists
    """, """
        If there are multiple paths with minimum length between the given endpoints,
        the choice of which one is returned is implementation dependent
    """)
    fun onePath() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
            }
        }
        then {
            hasShortestPath(graphA, "a", "b")
        }
    }

    @Eac("""
        A shortest path cannot contain non-trivial cycles
    """, """
        Indeed, replacing a cycle with its base point results in another path between the
        same start and end vertices with strictly shorter length
    """)
    fun graphWithCycle() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
                edge("b", "c")
                edge("c", "d")
                edge("d", "b")
                edge("b", "e")
            }
        }
        then {
            hasShortestPath(graphA, "a", "b", "e")
        }
    }
}
