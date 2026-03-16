package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.parser.ScenarioParser
import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.ScenarioWithQueries
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory
import org.junit.Assert
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.expect

@Deprecated("Use ScriptGenerationTesting")
open class ScriptGenerationTestHelper<S : BuildableScenario<AF>, AF : ActionFactory>(
    @Suppress("DEPRECATION") private val generatorFactory: () -> ScriptGenerator<AF, *, *, *, *, *>,
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

/**
 * Testing utilities for script generators.
 *
 * This class is flexible as to which kind of scenarios it takes as input.  Typically, you'll want to instantiate one
 * for each scenario type (verifiable, query, oracle) supported by your script generation adapter.
 */
open class ScriptGenerationTesting<S : BuildableScenario<*>>(
    val generator: (S) -> ScenarioScript,
    val parser: ScenarioParser<S> = SimpleScenarioParser(),
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
     *
     * Returns the regenerated scenario script, so that it can be used in further assertions.
     */
    fun checkScenarioGeneration(scenario: S, initContext: ScenarioParsingContext.() -> Unit = {}): ScenarioScript {
        Log.debug("Generating script")
        val generatedScript = generator(scenario)
        val renderedGeneratedScript = generatedScript.render()
        Log.debug("Generated:\n{}", renderedGeneratedScript)

        val parsedScenario = parser.parse(renderedGeneratedScript, initContext)

        Log.debug("Regenerating script")
        val regeneratedScript = generator(parsedScenario)
        val renderedRegeneratedScript = regeneratedScript.render()
        Log.debug("Regenerated:\n{}", renderedRegeneratedScript)

        expect(renderedRegeneratedScript) { renderedGeneratedScript }

        return regeneratedScript
    }

    /**
     * Asserts that two scenarios produce the same script, and returns the generated script.
     */
    fun assertScenariosProduceSameScript(expected: S, actual: S) = generator(expected).also {
        expect(it.render()) { generator(actual).render() }
    }

    companion object {

        private val Log = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}

/**
 * Creates a test helper for verifiable scenarios, given a way to parse them.
 */
fun <AF : ActionFactory, CF : CheckFactory> ScriptGenerationService<AF, CF, *, *, *, *, *>.createVerifiableTestHelper(
    parser: ScenarioParser<VerifiableScenario<AF, CF>>
) = ScriptGenerationTesting(::generateVerifiableScenario, parser)

/**
 * Creates a test helper for oracle scenarios, given a way to parse them.
 */
fun <AF : ActionFactory, VQF : QueryFactory, AGF: ActionGeneratorFactory> ScriptGenerationService<AF, *, *, VQF, AGF, *, *>.createOracleTestHelper(
    parser: ScenarioParser<OracleScenario<AF, VQF, AGF>>
) = ScriptGenerationTesting(::generateOracleScenario, parser)

/**
 * Creates a test helper for query scenarios, given a way to parse them.
 */
fun <AF : ActionFactory, QQF : QueryFactory> ScriptGenerationService<AF, *, QQF, *, *, *, *>.createQueryTestHelper(
    parser: ScenarioParser<ScenarioWithQueries<AF, QQF>>
) = ScriptGenerationTesting(::generateQueryScenario, parser)
