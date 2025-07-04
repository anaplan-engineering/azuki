package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.*
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
    private val declarationStateFactory: DeclarationStateFactory<S>,
    // These factories are needed only if generating oracle scenarios or scenarios with queries
    private val actionGeneratorFactory: AGF? = null,
    private val queryQueryFactory: QF? = null,
    private val verifyQueryFactory: QF? = null
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
            is OracleScenario<*, *, *> -> {
                val agf = checkNotNull(actionGeneratorFactory) {
                    "tried to generate an oracle scenario but no action generator factory was supplied"
                }

                val oracleScenario =
                    scenario as OracleScenario<AF, QF, AGF>
                val givenGenerate = generateGenerateScript(oracleScenario.givenActionGenerations(agf))
                val whenGenerate = generateGenerateScript(oracleScenario.whenActionGenerations(agf))

                val verify = generateVerifyScript(oracleScenario)

                generateOracleScenarioScript(given, whenever, givenGenerate, whenGenerate, verify)
            }
            is ScenarioWithQueries<*, *> -> {
                val query =
                    generateQueryScript(scenario as ScenarioWithQueries<AF, QF>)
                generateQueryScenarioScript(given, whenever, query)
            }
            else -> throw IllegalArgumentException("Unsupported scenario $scenario")
        }
    }

    open fun getChecks(scenario: VerifiableScenario<AF, CF>): List<ScriptGenerationCheck> =
        scenario.checks(checkFactory).map { it as ScriptGenerationCheck }

    open fun getValidationChecks(answer: ValidatableAnswer<*, CF>): List<ScriptGenerationCheck> =
        answer.createValidationChecks(checkFactory).map { it as ScriptGenerationCheck }

    open fun getChecksFromAnswer(answer: Answer<*, CF>): List<ScriptGenerationCheck> =
        answer.createChecks(checkFactory).map { it as ScriptGenerationCheck }

    fun generateThenScript(scenario: VerifiableScenario<AF, CF>) =
        generateThenScriptFromChecks(getChecks(scenario))

    fun generateThenScript(answers: List<Answer<*, CF>>, useValidationChecks: Boolean = false) =
        generateThenScriptFromChecks(answers.flatMap {
            if (useValidationChecks && it is ValidatableAnswer<*, *>) {
                getValidationChecks(it as ValidatableAnswer<*, CF>)
            } else {
                getChecksFromAnswer(it)
            }
        })

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
        scenario.commands(actionFactory).map { it as ScriptGenerationAction }


    fun generateGivenScript(scenario: BuildableScenario<AF>): String {
        val definitions = scenario.declarations(actionFactory)
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

    fun generateOracleScenarioScript(given: String, whenever: String, givenGenerate: String, whenGenerate: String, verify: String): String {
        return """
            oracleScenario {
                $given
                $givenGenerate
                $whenever
                $whenGenerate
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

    fun generateGenerateScript(generations: List<List<ActionGenerator>>) = try {
        generations.joinToString("\n") { generation ->
            val actionGenerators = generation.map { it as ScriptGenerationActionGenerator }
            if (actionGenerators.isEmpty()) {
                ""
            } else {
                """
                generate {
                    ${actionGenerators.joinToString("\n") { it.getActionGeneratorString() }}
                }
            """
            }
        }
    } catch (e: UnsupportedOperationException) {
        // TODO
        ""
    }

    fun generateVerifyScript(scenario: ScenarioWithQueries<AF, QF>): String {
        val qf = checkNotNull(verifyQueryFactory) { "Tried to generate a verify script but no query factory was supplied" }
        return generateVerifyOrQueryScript(scenario.queries(qf), blockName = "verify")
    }

    fun generateQueryScript(scenario: ScenarioWithQueries<AF, QF>): String {
        val qf = checkNotNull(queryQueryFactory) { "Tried to generate a query script but no query factory was supplied" }
        return generateVerifyOrQueryScript(scenario.queries(qf), blockName = "query")
    }

    private fun generateVerifyOrQueryScript(scenarioQueries: ScenarioQueries, blockName: String): String {
        if (scenarioQueries.isEmpty()) {
            throw IllegalArgumentException("No queries for query scenario")
        }

        val queries = scenarioQueries.queries.joinToString("\n") { (it as ScriptGenerationQuery<*>).getQueryScript() }
        val forAllQueries =
            scenarioQueries.forAllQueries.joinToString("\n") { (it as ScriptGenerationDerivedQuery<*>).getDerivedQueryScript() }

        return """
            $blockName {
                $queries
                $forAllQueries
            }
        """
    }

    companion object {
        private val declarationBuilderFactory =
            DeclarationBuilderFactory(ScriptGenDeclarationBuilderFactory::class.java)
    }
}
