package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.graphs.dsl.verifiableScenario
import org.junit.Test

class GraphScriptGeneratorTest {

    @Test
    fun graphWithEdges() {
        ScenarioScriptingTestUtils.checkScenarioGeneration(
            verifiableScenario {
                given {
                    thereIsAGraph("graphA") {
                        edge("a", "b")
                    }
                }
                then {
                    hasVertexCount("graphA", 2)
                }
            }
        )
    }
}
