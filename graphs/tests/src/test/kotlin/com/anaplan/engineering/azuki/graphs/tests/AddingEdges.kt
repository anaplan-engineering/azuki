package com.anaplan.engineering.azuki.graphs.tests

import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphScenario

class AddingEdges : GraphScenario() {

    companion object {
        const val graphA = "graphA"
    }

    @AnalysisScenario
    fun addEdges1() {
        given {
            thereIsAGraph(graphA) {
                edge("a", "b")
            }
        }
        whenever {
            addEdge(graphA, "b", "c")
        }
        then {
            hasVertexCount(graphA, 3)
        }
        successor {
            whenever {
                addEdge(graphA, "a", "c")
            }
            then {
                hasVertexCount(graphA, 3)
            }
        }
    }

    @AnalysisScenario
    fun addEdges2() {
        given {
            thereIsAGraph(graphA) {
                edge("a", "b")
            }
        }
        whenever {
            addEdge(graphA, "b", "c")
        }
        then {
            hasVertexCount(graphA, 3)
        }
        successors(
            {
                whenever {
                    addEdge(graphA, "a", "c")
                }
                then {
                    hasVertexCount(graphA, 3)
                }
            },
            {
                whenever {
                    addEdge(graphA, "d", "a")
                }
                then {
                    hasVertexCount(graphA, 4)
                }
            }
        )
    }

    @AnalysisScenario
    fun addEdges3() {
        given {
            thereIsAGraph(graphA) {
                edge("a", "b")
            }
        }
        successors(
            {
                whenever {
                    addEdge(graphA, "a", "c")
                }
                then {
                    hasVertexCount(graphA, 3)
                }
            },
            {
                whenever {
                    addEdge(graphA, "d", "a")
                }
                then {
                    hasVertexCount(graphA, 4)
                }
            }
        )
    }
}
