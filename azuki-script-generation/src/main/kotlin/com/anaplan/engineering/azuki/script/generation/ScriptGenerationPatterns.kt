package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.ScenarioWithQueries
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory

/**
 * Implements stock patterns for the script generation service.
 */
class ScriptGenerationPatterns<out AF : ActionFactory, out CF : CheckFactory, out QF : QueryFactory, out AGF : ActionGeneratorFactory>(
    private val service: ScriptGenerationService<AF, CF, QF, AGF, *, *>
) {

    /**
     * Generates a verifiable scenario script.
     */
    fun verifiableScenario(scenario: VerifiableScenario<in AF, in CF>) = service.given {
        fromScenario(scenario)
    }.whenever {
        fromScenario(scenario)
    }.then {
        fromScenario(scenario)
    }.verifiableScenario()

    /**
     * Generates an incomplete scenario script from an oracle.
     */
    fun incompleteScenarioFromOracle(oracle: OracleScenario<in AF, in QF, in AGF>) = service.given {
        fromScenario(oracle)
    }.whenever {
        fromScenario(oracle)
    }.incompleteScenario()

    /**
     * Generates a verifiable script using the setup from an oracle and checks from a corresponding collection of answers.
     */
    fun verifiableScenarioFromOracle(
        oracle: BuildableScenario<in AF>, answers: Collection<Answer<*, in CF>>
    ) = service.given {
        fromScenario(oracle)
    }.whenever {
        fromScenario(oracle)
    }.then {
        fromAnswers(answers)
    }.verifiableScenario()

    /**
     * Generates a script for a query scenario.
     */
    fun queryScenario(scenario: ScenarioWithQueries<in AF, in QF>) = service.given {
        fromScenario(scenario)
    }.whenever {
        fromScenario(scenario)
    }.query {
        fromScenario(scenario)
    }.queryScenario()

    /**
     * Builds the blocks for an oracle scenario, but stops short of finishing the build.
     */
    fun oracleScenarioBuilder(scenario: OracleScenario<in AF, in QF, in AGF>) = service.given {
        fromScenario(scenario)
    }.generate {
        blocksFromScenario(scenario)
    }.whenever {
        fromScenario(scenario)
    }.generate {
        blocksFromScenario(scenario)
    }.verify {
        fromScenario(scenario)
    }

    /**
     * Generates a script for an oracle scenario.
     */
    fun oracleScenario(scenario: OracleScenario<in AF, in QF, in AGF>) =
        oracleScenarioBuilder(scenario).oracleScenario()

    /**
     * Generates a script for a scenario whose kind (verifiable, oracle, query) isn't known until run-time.
     *
     * For type safety, this wrapper takes projection methods to try map the base scenario type to all the specific
     * scenario kinds the generator supports.  These should usually be implemented as `{ this as? NarrowScenarioType }`.
     * If a projection method is not given, scenarios of that kind won't be handled and will result in an exception.
     */
    fun <S : BuildableScenario<in AF>> scenario(
        scenario: S,
        asVerifiable: S.() -> VerifiableScenario<in AF, in CF>? = { null },
        asOracle: S.() -> OracleScenario<in AF, in QF, in AGF>? = { null },
        asQuery: S.() -> ScenarioWithQueries<in AF, in QF>? = { null },
    ) = listOf<S.() -> ScenarioScript?>({ asVerifiable()?.let { service.patterns.verifiableScenario(it) } },
        { asOracle()?.let { service.patterns.oracleScenario(it) } },
        { asQuery()?.let { service.patterns.queryScenario(it) } },
        { throw IllegalArgumentException("unsupported scenario type: ${this::class.simpleName}") }).firstNotNullOf {
        it(scenario)
    }
}
