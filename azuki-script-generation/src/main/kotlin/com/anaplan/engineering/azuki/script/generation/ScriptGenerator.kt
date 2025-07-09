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
) {

    fun generateScript(scenario: BuildableScenario<AF>): String {
        val script = generateUnformattedScript(scenario)
        return ScenarioFormatter.formatScenario(script)
    }

    private fun generateUnformattedScript(scenario: BuildableScenario<AF>): String {
        val given = generateGivenScript(scenario)
        val whenever = generateWheneverScript(scenario)

        return if (scenario is VerifiableScenario<*, *>) {
            @Suppress("UNCHECKED_CAST") val then = generateThenScript(scenario as VerifiableScenario<AF, CF>)
            generateVerifiableScenarioScript(given, whenever, then)
        } else {
            generateNonVerifiableScenario(given, whenever, scenario)
        }
    }

    protected open fun generateNonVerifiableScenario(
        given: String, whenever: String, scenario: BuildableScenario<AF>
    ): String = throw IllegalArgumentException("Unsupported scenario $scenario")

    open fun getChecks(scenario: VerifiableScenario<AF, CF>): List<Check> =
        scenario.checks(checkFactory)

    open fun getValidationChecks(answer: ValidatableAnswer<*, CF>): List<Check> =
        answer.createValidationChecks(checkFactory)

    open fun getChecksFromAnswer(answer: Answer<*, CF>): List<Check> =
        answer.createChecks(checkFactory)

    protected open fun thenBuilder(): ThenBuilder = SimpleThenBuilder()

    private fun generateThenScript(scenario: VerifiableScenario<AF, CF>) =
        generateThenScriptFromChecks(getChecks(scenario))

    fun generateThenScript(answers: List<Answer<*, CF>>, useValidationChecks: Boolean = false) =
        generateThenScriptFromChecks(answers.flatMap {
            if (useValidationChecks && it is ValidatableAnswer<*, *>) {
                getValidationChecks(it as ValidatableAnswer<*, CF>)
            } else {
                getChecksFromAnswer(it)
            }
        })

    fun generateThenScriptFromChecks(checks: List<Check>): String {
        val body = thenBuilder().apply { addChecks(checks) }.build()

        return if (body.isEmpty()) {
            throw IllegalArgumentException("No checks to generate!")
        } else {
            """
                then {
                    ${body.joinToString("\n") { it.getCheckScript() }}
                }
            """
        }
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

    open fun getDeclarationActions(scenario: BuildableScenario<AF>): List<Action> =
        scenario.declarations(actionFactory)

    open fun getBuildActions(scenario: BuildableScenario<AF>): List<ScriptGenerationAction> =
        scenario.commands(actionFactory).map(Action::toScriptGenAction)

    fun generateGivenScript(scenario: BuildableScenario<AF>): String =
        generateGivenScriptFromActions(getDeclarationActions(scenario))

    fun generateGivenScriptFromActions(definitions: List<Action>): String {
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

    fun generateWheneverScript(scenario: BuildableScenario<AF>) =
        generateWheneverScriptFromActions(getBuildActions(scenario))

    fun generateWheneverScriptFromActions(buildActions: List<ScriptGenerationAction>) =
        if (buildActions.isEmpty()) {
            ""
        } else {
            """
                whenever {
                    ${buildActions.joinToString("\n") { it.getActionScript() }}
                }
            """
        }

    @Deprecated(
        message="Use the version with separate generate blocks",
        replaceWith = ReplaceWith("""generateOracleScenarioScript(given, whenever, "", generate, verify)""")
    )
    fun generateOracleScenarioScript(given: String, whenever: String, generate: String, verify: String) =
        generateOracleScenarioScript(given, whenever, "", generate, verify)

    fun generateOracleScenarioScript(given: String, whenever: String, givenGenerate: String, whenGenerate: String, verify: String) =
        """
            oracleScenario {
                $given
                $givenGenerate
                $whenever
                $whenGenerate
                $verify
            }
        """

    fun generateQueryScenarioScript(given: String, whenever: String, query: String) =
        """
            queryScenario {
                $given
                $whenever
                $query
            }
        """

    companion object {
        private val declarationBuilderFactory =
            DeclarationBuilderFactory(ScriptGenDeclarationBuilderFactory::class.java)
    }
}

abstract class VerificationCapableScriptGenerator<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    S : DeclarationState,
    >(
    actionFactory: AF,
    checkFactory: CF,
    declarationStateFactory: DeclarationStateFactory<S>,
    private val actionGeneratorFactory: AGF,
    private val queryQueryFactory: QF,
    private val verifyQueryFactory: QF
    ): ScriptGenerator<AF, CF, QF, AGF, S>(
        actionFactory,
        checkFactory,
        declarationStateFactory,
    ) {

    @Suppress("UNCHECKED_CAST")
    override fun generateNonVerifiableScenario(
        given: String,
        whenever: String,
        scenario: BuildableScenario<AF>
    ): String =
        when (scenario) {
            is OracleScenario<*, *, *> -> {
                val oracleScenario = scenario as OracleScenario<AF, QF, AGF>
                val givenGenerate =
                    generateGenerateScript(oracleScenario.givenActionGenerations(actionGeneratorFactory))
                val whenGenerate = generateGenerateScript(oracleScenario.whenActionGenerations(actionGeneratorFactory))
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

    fun generateGenerateScript(generations: List<List<ActionGenerator>>) =
        generations.joinToString("\n") { generation ->
            val actionGenerators = generation.map { it as ScriptGenerationActionGenerator }
            if (actionGenerators.isEmpty()) {
                ""
            } else {
                """
                generate {
                    ${actionGenerators.joinToString("\n") { it.getActionGeneratorScript() }}
                }
            """
            }
        }

    fun generateVerifyScript(scenario: OracleScenario<AF, QF, AGF>) =
        generateVerifyScriptFromQueries(scenario.queries(verifyQueryFactory))

    fun generateVerifyScriptFromQueries(scenarioQueries: ScenarioQueries) =
        if (scenarioQueries.isEmpty()) {
            throw IllegalArgumentException("No queries for oracle scenario")
        } else {
            """
            verify {
                ${generateVerifyOrQueryScriptBody(scenarioQueries)}
            }
            """
        }

    fun generateQueryScript(scenario: ScenarioWithQueries<AF, QF>) =
        generateQueryScriptFromQueries(scenario.queries(queryQueryFactory))

    fun generateQueryScriptFromQueries(scenarioQueries: ScenarioQueries) =
        if (scenarioQueries.isEmpty()) {
            throw IllegalArgumentException("No queries for verify script")
        } else {
            """
            query {
                ${generateVerifyOrQueryScriptBody(scenarioQueries)}
            }
            """
        }

    private fun generateVerifyOrQueryScriptBody(scenarioQueries: ScenarioQueries): String {
        val queries = scenarioQueries.queries.joinToString("\n") { (it as ScriptGenerationQuery<*>).getQueryScript() }
        val forAllQueries =
            scenarioQueries.forAllQueries.joinToString("\n") { (it as ScriptGenerationDerivedQuery<*>).getDerivedQueryScript() }
        return queries + "\n" + forAllQueries
    }
}
