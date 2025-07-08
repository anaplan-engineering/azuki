package com.anaplan.engineering.azuki.graphs.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.dsl.GraphBuildableScenario
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationTestHelper

object ScenarioScriptingTestUtils : ScriptGenerationTestHelper<GraphBuildableScenario, GraphActionFactory<*>>(
    generator = GraphScriptGenerator,
    parser = object : SimpleScenarioParser<GraphBuildableScenario>() {
        override val defaultImports: ScenarioParsingContext.() -> Unit = {
            import("com.anaplan.engineering.azuki.graphs.dsl.*")
            import("com.anaplan.engineering.azuki.graphs.*")
        }
    }
)
