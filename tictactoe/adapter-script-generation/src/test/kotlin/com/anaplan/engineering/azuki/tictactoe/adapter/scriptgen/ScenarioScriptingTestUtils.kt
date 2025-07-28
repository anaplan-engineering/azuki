package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationTestHelper
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeBuildableScenario

object ScenarioScriptingTestUtils : ScriptGenerationTestHelper<TicTacToeBuildableScenario, TicTacToeActionFactory>(
    generator = TicTacToeScriptGenerator,
    parser = object : SimpleScenarioParser<TicTacToeBuildableScenario>() {
        override val defaultImports: ScenarioParsingContext.() -> Unit = {
            import("com.anaplan.engineering.azuki.tictactoe.dsl.*")
            import("com.anaplan.engineering.azuki.tictactoe.*")
        }
    })

