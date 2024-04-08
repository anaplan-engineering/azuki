package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.dsl.verifiableScenario
import org.junit.Test

class GraphScriptGeneratorTest {

    companion object {
        private val graphA = "graphA"
    }

    @Test
    fun graphWithEdges() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(
            verifiableScenario {
                given {
                    thereIsAnUndirectedGraph(graphA) {
                        edge("a", "b")
                    }
                }
                then {
                    hasVertexCount(graphA, 2)
                }
            }
        )
    }

    @Test
    fun graphWithParallelBlock() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(
            verifiableScenario {
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
        )
    }

    @Test
    fun directedGraph() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(
            verifiableScenario {
                given {
                    thereIsADirectedGraph("graphA") {}
                }
                then {
                    hasVertexCount("graphA", 0)
                }
            }
        )
    }
}
