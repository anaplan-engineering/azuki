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
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
            }
        }
        whenever {
            addEdgeToUndirectedGraph(graphA, "b", "c")
        }
        then {
            hasVertexCount(graphA, 3)
        }
        successor {
            whenever {
                addEdgeToUndirectedGraph(graphA, "a", "c")
            }
            then {
                hasVertexCount(graphA, 3)
            }
        }
    }

    @AnalysisScenario
    fun addEdges2() {
        given {
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
            }
        }
        whenever {
            addEdgeToUndirectedGraph(graphA, "b", "c")
        }
        then {
            hasVertexCount(graphA, 3)
        }
        successors(
            {
                whenever {
                    addEdgeToUndirectedGraph(graphA, "a", "c")
                }
                then {
                    hasVertexCount(graphA, 3)
                }
            },
            {
                whenever {
                    addEdgeToUndirectedGraph(graphA, "d", "a")
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
            thereIsAnUndirectedGraph(graphA) {
                edge("a", "b")
            }
        }
        successors(
            {
                whenever {
                    addEdgeToUndirectedGraph(graphA, "a", "c")
                }
                then {
                    hasVertexCount(graphA, 3)
                }
            },
            {
                whenever {
                    addEdgeToUndirectedGraph(graphA, "d", "a")
                }
                then {
                    hasVertexCount(graphA, 4)
                }
            }
        )
    }

    @AnalysisScenario
    fun addEdges4() {
        given {
            thereIsAnUndirectedGraph(graphA)
        }
        repeat(10) { i ->
            whenever {
                addEdgeToUndirectedGraph(graphA, i, i + 1)
            }
            then {
                hasVertexCount(graphA, i + 1L)
            }
        }
    }

    @AnalysisScenario
    fun addEdges5() {
        given {
            thereIsAnUndirectedGraph(graphA)
        }
        whenever {
            parallel({
                addEdgeToUndirectedGraph(graphA, "a", "b")
            }, {
                addEdgeToUndirectedGraph(graphA, "a", "c")
            })
        }
        then {
            hasVertexCount(graphA, 3L)
        }
    }
}
