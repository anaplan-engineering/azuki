package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.parser.ScenarioParser
import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import org.junit.Assert
import org.slf4j.LoggerFactory

open class ScriptGenerationTestHelper<S: BuildableScenario<AF>, AF: ActionFactory>(
    private val generator: ScriptGenerator<AF, *, *, *, *, *>,
    private val parser: ScenarioParser<S> = SimpleScenarioParser(),
) {
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
    fun checkScenarioGeneration(scenario: S, initContext: ScenarioParsingContext.() -> Unit = {}) {
        Log.debug("Generating script")
        val generatedScript = generator.generateScript(scenario)
        Log.debug("Generated:\n$generatedScript")

        val parsedScenario = parser.parse(generatedScript, initContext)

        Log.debug("Regenerating script")
        val regeneratedScript = generator.generateScript(parsedScenario)
        Log.debug("Regenerated:\n$regeneratedScript")

        Assert.assertEquals(generatedScript, regeneratedScript)
    }

    companion object {
        private val Log = LoggerFactory.getLogger(ScriptGenerationTestHelper::class.java)
    }
}

