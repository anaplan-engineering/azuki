package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeBuildableScenario
import org.junit.Assert
import org.slf4j.LoggerFactory

object ScenarioScriptingTestUtils {

    /**
     * Verify script correctness by generating, parsing and regenerating.
     *
     * If there are no exceptions then all constructs in initial scenario will have been supported. If the two scripts
     * generated are identical then we know the initial scenario and parsed scenario are semantically equivalent.
     *
     * Note that, there may be some differences between the initial dsl and that generated, but they will produce the
     * same actions, checks, and queries.
     *
     * printScript will print the initially generated script to assist debugging
     */
    fun checkScenarioGeneration(scenario: TicTacToeBuildableScenario) {
        Log.debug("Generating script")
        val generatedScript = TicTacToeScriptGenerator.generateScript(scenario)
        Log.debug("Generated:\n{}", generatedScript)

        val parsedScenario = SimpleScenarioParser<TicTacToeBuildableScenario>().parse(
            generatedScript
        ) {
            import("com.anaplan.engineering.azuki.tictactoe.dsl.*")
            import("com.anaplan.engineering.azuki.tictactoe.*")
        }

        Log.debug("Regenerating script")
        val regeneratedScript = TicTacToeScriptGenerator.generateScript(parsedScenario)
        Log.debug("Regenerated:\n{}", regeneratedScript)

        Assert.assertEquals(generatedScript, regeneratedScript)
    }

    private val Log = LoggerFactory.getLogger(ScenarioScriptingTestUtils::class.java)
}

