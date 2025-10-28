package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.parser.ScenarioParser
import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.CheckFactory
import org.junit.Assert
import org.slf4j.LoggerFactory
import kotlin.test.expect

open class ScriptGenerationTestHelper<S : BuildableScenario<AF>, AF : ActionFactory>(
    private val generatorFactory: () -> ScriptGenerator<AF, *, *, *, *, *>,
    private val parser: ScenarioParser<S> = SimpleScenarioParser(),
) {
    /**
     * Round-trip the generator and parser by generating, parsing, and regenerating.
     *
     * This checks that the generator handles all the constructs in the scenario, the parser can process the generated
     * script, and the generator converts the parsed scenario to a script identical to the one from which it came.
     *
     * The initial and parsed scenario are not necessarily syntactically equivalent, as generation may have produced a
     * different DSL.  The two should be semantically equivalent (have the same actions, checks, queries, and so on),
     * but we can't check that here.
     */
    fun checkScenarioGeneration(scenario: S, initContext: ScenarioParsingContext.() -> Unit = {}) {
        Log.debug("Generating script")
        val generatedScript = generatorFactory().generateScript(scenario)
        Log.debug("Generated:\n$generatedScript")

        val parsedScenario = parser.parse(generatedScript, initContext)

        // The two generators need to be separate to avoid sharing environment
        Log.debug("Regenerating script")
        val regeneratedScript = generatorFactory().generateScript(parsedScenario)
        Log.debug("Regenerated:\n$regeneratedScript")

        Assert.assertEquals(generatedScript, regeneratedScript)
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}

class ScriptGeneratorTestHelper<AF : ActionFactory, CF : CheckFactory, SC : VerifiableScenario<AF, CF>>(
    val generator: ScriptGenerationService<AF, CF, *, *, *, *>, val parser: ScenarioParser<SC> = SimpleScenarioParser()
) {

    /**
     * Round-trip the generator and parser by generating, parsing, and regenerating.
     *
     * This checks that the generator handles all the constructs in the scenario, the parser can process the generated
     * script, and the generator converts the parsed scenario to a script identical to the one from which it came.
     *
     * The initial and parsed scenario are not necessarily syntactically equivalent, as generation may have produced a
     * different DSL.  The two should be semantically equivalent (have the same actions, checks, queries, and so on),
     * but we can't check that here.
     */
    fun checkScenarioGeneration(scenario: SC, initContext: ScenarioParsingContext.() -> Unit = {}) {
        Log.debug("Generating script")
        val generatedScript = generator.verifiableScenario(scenario).renderFormatted()
        Log.debug("Generated:\n{}", generatedScript)

        val parsedScenario = parser.parse(generatedScript, initContext)

        // The two generators need to be separate to avoid sharing environment
        Log.debug("Regenerating script")
        val regeneratedScript = generator.verifiableScenario(scenario).renderFormatted()
        Log.debug("Regenerated:\n{}", regeneratedScript)

        expect(regeneratedScript) { generatedScript }
    }

    companion object {

        private val Log = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
