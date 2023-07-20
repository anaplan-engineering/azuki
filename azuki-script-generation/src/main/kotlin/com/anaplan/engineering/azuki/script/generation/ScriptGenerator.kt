package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter

abstract class ScriptGenerator<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    S : DeclarationState,
    >(
    private val actionFactory: AF,
    private val checkFactory: CF,
    private val declarationStateFactory: DeclarationStateFactory<S>
) {

    fun generateScript(scenario: BuildableScenario<AF>): String {
        val script = generateUnformattedScript(scenario)
        return ScenarioFormatter.formatScenario(script)
    }

    private fun generateUnformattedScript(scenario: BuildableScenario<AF>): String {
        val given = generateGivenScript(scenario)
        val whenever = generateWheneverScript(scenario)
        @Suppress("UNCHECKED_CAST")
        return when (scenario) {
            is VerifiableScenario<*, *> -> {
                val then = generateThenScript(scenario as VerifiableScenario<AF, CF>)
                generateVerifiableScenarioScript(given, whenever, then)
            }

//            is OracleScenario<*, *, *> -> {
//                val oracleScenario =
//                    scenario as OracleScenario<AF, QF, AGF>
//                val generate =
//                    generateGenerateScript(oracleScenario)
//                val verify =
//                    generateVerifyScript(oracleScenario)
//
//                generateOracleScenarioScript(given, whenever, generate, verify)
//            }
//
//            is ScenarioWithQueries<*, *> -> {
//                val query =
//                    generateQueryScript(scenario as ScenarioWithQueries<AF, QF>)
//                generateQueryScenarioScript(given, whenever, query)
//            }

            else -> throw IllegalArgumentException("Unsupported scenario $scenario")
        }
    }

    open fun getChecks(scenario: VerifiableScenario<AF, CF>): List<ScriptGenerationCheck> =
        scenario.checks(checkFactory).map { it as ScriptGenerationCheck }


    fun generateThenScript(scenario: VerifiableScenario<AF, CF>) =
        generateThenScriptFromChecks(getChecks(scenario))

    fun generateThenScript(answers: List<Answer<*, CF>>) =
        generateThenScriptFromChecks(answers.flatMap { it.createChecks(checkFactory) }
            .map { it as ScriptGenerationCheck })

    private fun generateThenScriptFromChecks(checks: List<ScriptGenerationCheck>) =
        if (checks.isEmpty()) {
            throw IllegalArgumentException("No checks to generate!")
        } else {
            """
                then {
                    ${checks.joinToString("\n") { it.getCheckScript() }}
                }
            """
        }

    fun generateVerifiableScenarioScript(given: String, whenever: String, then: String): String {
        return """
            verifiableScenario {
                $given
                $whenever
                $then
            }
        """
    }

    open fun getBuildActions(scenario: BuildableScenario<AF>): List<ScriptGenerationAction> =
        scenario.buildActions(actionFactory).map { it as ScriptGenerationAction }


    fun generateGivenScript(scenario: BuildableScenario<AF>): String {
        val definitions = scenario.definitions(actionFactory)
        if (definitions.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            definitions.forEach { println(" * $it") }
            throw IllegalArgumentException("Scriptgen is missing action")
        }
        val declarableActions = definitions.map { toDeclarableAction<S>(it) }
        val declarations = DeclarationStateBuilder(declarationStateFactory).build(declarableActions)
        return if (declarations.isEmpty()) {
            ""
        } else {
            """
                given {
                    ${declarations.joinToString("\n") { declarationBuilder(it).getDeclarationScript() }}
                }
            """
        }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, ScriptGenDeclarationBuilder<D>>(declaration)

    fun generateWheneverScript(scenario: BuildableScenario<AF>): String {
        val buildActions = getBuildActions(scenario)
        return if (buildActions.isEmpty()) {
            ""
        } else {
            """
                whenever {
                    ${buildActions.joinToString("\n") { it.getActionScript() }}
                }
            """
        }
    }

    fun generateOracleScenarioScript(given: String, whenever: String, generate: String, verify: String): String {
        return """
            oracleScenario {
                $given
                $whenever
                $generate
                $verify
            }
        """
    }

    fun generateQueryScenarioScript(given: String, whenever: String, query: String): String {
        return """
            queryScenario {
                $given
                $whenever
                $query
            }
        """
    }

    companion object {
        private val declarationBuilderFactory =
            DeclarationBuilderFactory(ScriptGenDeclarationBuilderFactory::class.java)
    }
}
