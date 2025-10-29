package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.ScenarioWithQueries
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory

/**
 * Generates a verifiable scenario script.
 */
fun <AF : ActionFactory, CF : CheckFactory> ScriptGenerationService<AF, CF, *, *, *, *>.verifiableScenario(scenario: VerifiableScenario<in AF, in CF>) =
    given {
        fromScenario(scenario)
    }.whenever {
        fromScenario(scenario)
    }.then {
        fromScenario(scenario)
    }.verifiableScenario()

/**
 * Generates an incomplete scenario script from an oracle.
 */
fun <AF : ActionFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> ScriptGenerationService<AF, *, *, *, QF, AGF>.incompleteScenarioFromOracle(
    oracle: OracleScenario<in AF, in QF, in AGF>
) = given {
    fromScenario(oracle)
}.whenever {
    fromScenario(oracle)
}.incompleteScenario()

/**
 * Generates a verifiable script using the setup from an oracle and checks from a corresponding collection of answers.
 */
fun <AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> ScriptGenerationService<AF, CF, *, *, QF, AGF>.verifiableScenarioFromOracle(
    oracle: OracleScenario<in AF, in QF, in AGF>, answers: Collection<Answer<*, in CF>>
) = given {
    fromScenario(oracle)
}.whenever {
    fromScenario(oracle)
}.then {
    fromAnswers(answers)
}.verifiableScenario()

/**
 * Generates a script for a query scenario.
 */
fun <AF : ActionFactory, QF : QueryFactory> ScriptGenerationService<AF, *, *, *, QF, *>.queryScenario(
    scenario: ScenarioWithQueries<in AF, in QF>
) = given {
    fromScenario(scenario)
}.whenever {
    fromScenario(scenario)
}.query {
    fromScenario(scenario)
}.queryScenario()

/**
 * Generates a script for an oracle scenario.
 */
fun <AF : ActionFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> ScriptGenerationService<AF, *, *, *, QF, AGF>.oracleScenario(
    scenario: OracleScenario<in AF, in QF, in AGF>
) = given {
    fromScenario(scenario)
}.generate {
    blocksFromScenario(scenario)
}.whenever {
    fromScenario(scenario)
}.generate {
    blocksFromScenario(scenario)
}.verify {
    fromScenario(scenario)
}.oracleScenario()
