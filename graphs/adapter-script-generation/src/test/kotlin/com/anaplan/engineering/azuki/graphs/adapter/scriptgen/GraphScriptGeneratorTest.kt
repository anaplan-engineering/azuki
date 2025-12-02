package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.graphs.dsl.GraphBuildableScenario
import com.anaplan.engineering.azuki.graphs.dsl.GraphVerifiableScenario
import com.anaplan.engineering.azuki.graphs.dsl.verifiableScenario
import com.anaplan.engineering.azuki.script.generation.createVerifiableTestHelper
import org.junit.Test

class GraphScriptGeneratorTest {

    companion object {
        const val graphA = "graphA"

        val VerifiableScenarioTesting =
            GraphScriptGeneration.createVerifiableTestHelper(GraphScenarioParser<GraphVerifiableScenario>())
    }

    @Test
    fun graphWithEdges() {
        VerifiableScenarioTesting.checkScenarioGeneration(verifiableScenario {
            given {
                thereIsAnUndirectedGraph(graphA) {
                    edge("a", "b")
                }
            }
            then {
                hasVertexCount(graphA, 2)
            }
        })
    }

    @Test
    fun graphWithParallelBlock() {
        VerifiableScenarioTesting.checkScenarioGeneration(verifiableScenario {
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
        })
    }

    @Test
    fun directedGraph() {
        VerifiableScenarioTesting.checkScenarioGeneration(verifiableScenario {
            given {
                thereIsADirectedGraph("graphA") {}
            }
            then {
                hasVertexCount("graphA", 0)
            }
        })
    }
}

class GraphScenarioParser<S : GraphBuildableScenario> : SimpleScenarioParser<S>() {

    override val defaultImports: ScenarioParsingContext.() -> Unit = {
        import("com.anaplan.engineering.azuki.graphs.dsl.*")
        import("com.anaplan.engineering.azuki.graphs.*")
    }
}
