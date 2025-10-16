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
    E : CheckComposingScriptGenerationEnvironment<E>,
    >(
    private val actionFactory: AF,
    private val checkFactory: CF,
    private val declarationStateFactory: DeclarationStateFactory<S>,
    private val environmentFactory: ScriptGenerationEnvironmentFactory<E>,
) {
    fun generateScript(scenario: BuildableScenario<AF>, customEnvironment: E? = null): String {
        val script = generateUnformattedScript(scenario, customEnvironment ?: environmentFactory.create())
        return ScenarioFormatter.formatScenario(script)
    }

    private fun generateUnformattedScript(scenario: BuildableScenario<AF>, environment: E): String {
        val given = generateGivenScript(scenario, environment)
        val whenever = generateWheneverScript(scenario, environment)

        return if (scenario is VerifiableScenario<*, *>) {
            @Suppress("UNCHECKED_CAST") val then =
                generateThenScript(scenario as VerifiableScenario<AF, CF>, environment)
            generateVerifiableScenarioScript(given, whenever, then)
        } else {
            generateNonVerifiableScenario(given, whenever, scenario)
        }
    }

    protected open fun generateNonVerifiableScenario(
        given: String, whenever: String, scenario: BuildableScenario<AF>
    ): String = throw UnsupportedOperationException("Unsupported scenario type ${scenario::class}")

    fun getChecks(scenario: VerifiableScenario<AF, CF>): List<Check> = scenario.checks(checkFactory)

    fun getValidationChecks(answer: ValidatableAnswer<*, CF>): List<Check> = answer.createValidationChecks(checkFactory)

    fun getChecksFromAnswer(answer: Answer<*, CF>): List<Check> = answer.createChecks(checkFactory)

    fun generateThenScript(scenario: VerifiableScenario<AF, CF>, customEnvironment: E? = null) =
        generateThenScriptFromChecks(getChecks(scenario), customEnvironment)

    fun generateThenScript(answers: List<Answer<*, CF>>, customEnvironment: E? = null, useValidationChecks: Boolean = false) =
        generateThenScriptFromChecks(answers.flatMap {
            if (useValidationChecks && it is ValidatableAnswer<*, *>) {
                getValidationChecks(it as ValidatableAnswer<*, CF>)
            } else {
                getChecksFromAnswer(it)
            }
        }, customEnvironment)

    fun generateThenScriptFromChecks(checks: List<Check>, customEnvironment: E? = null): String {
        val environment = customEnvironment ?: environmentFactory.createForThen()

        checks.forEach {
            require(it !is UnsupportedCheck) { "unsupported check: $it" }
            require(it is ScriptGenerationCheck<*>) { "check $it is not a ScriptGenerationCheck" }
        }

        val basicChecks = checks.filterIsInstance<ScriptGenerationCheck<E>>().mapNotNull {
            if (it is ComposableScriptGenerationCheck<E>) {
                it.composeInto(environment)
                null
            } else {
                it
            }
        }

        val allChecks = environment.composedChecks + basicChecks
        return if (allChecks.isEmpty()) {
            throw IllegalArgumentException("No checks to generate!")
        } else {
            """
                then {
                    ${allChecks.joinToString("\n") { it.getCheckScript(environment) }}
                }
            """
        }
    }

    fun generateVerifiableScenarioScript(given: String, whenever: String, then: String) = """
            verifiableScenario {
                $given
                $whenever
                $then
            }
        """

    fun getDeclarationActions(scenario: BuildableScenario<AF>): List<Action> = scenario.declarations(actionFactory)

    fun getBuildActions(scenario: BuildableScenario<AF>): List<ScriptGenerationAction<E>> =
        scenario.commands(actionFactory).map(Action::toScriptGenAction)

    fun generateGivenScript(scenario: BuildableScenario<AF>, customEnvironment: E? = null): String =
        generateGivenScriptFromActions(getDeclarationActions(scenario), customEnvironment)

    fun generateGivenScriptFromActions(definitions: List<Action>, customEnvironment: E? = null): String {
        val environment = customEnvironment ?: environmentFactory.createForGiven()

        if (definitions.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
            definitions.forEach { println(" * $it") }
            throw IllegalArgumentException("Scriptgen is missing action")
        }
        val declarableActions = definitions.map { toDeclarableAction<S>(it) }
        val declarationBuilders =
            DeclarationStateBuilder(declarationStateFactory).build(declarableActions).map { declarationBuilder(it) }

        return if (declarationBuilders.isEmpty()) {
            ""
        } else {
            """
                given {
                    ${declarationBuilders.joinToString("\n") { it.getDeclarationScript(environment) }}
                }
            """
        }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, ScriptGenerationDeclarationBuilder<E, D>>(declaration)

    fun generateWheneverScript(scenario: BuildableScenario<AF>, customEnvironment: E? = null) =
        generateWheneverScriptFromActions(getBuildActions(scenario), customEnvironment)

    fun generateWheneverScriptFromActions(buildActions: List<ScriptGenerationAction<E>>, customEnvironment: E? = null) =
        if (buildActions.isEmpty()) {
            ""
        } else {
            val environment = customEnvironment ?: environmentFactory.createForWhenever()
            """
            whenever {
                ${buildActions.joinToString("\n") { it.getActionScript(environment) }}
            }
            """
        }

    @Deprecated(message = "Use the version with separate generate blocks",
        replaceWith = ReplaceWith("""generateOracleScenarioScript(given, whenever, "", generate, verify)"""))
    fun generateOracleScenarioScript(given: String, whenever: String, generate: String, verify: String) =
        generateOracleScenarioScript(given, whenever, "", generate, verify)

    fun generateOracleScenarioScript(
        given: String, whenever: String, givenGenerate: String, whenGenerate: String, verify: String
    ) = """
            oracleScenario {
                $given
                $givenGenerate
                $whenever
                $whenGenerate
                $verify
            }
        """

    fun generateQueryScenarioScript(given: String, whenever: String, query: String) = """
            queryScenario {
                $given
                $whenever
                $query
            }
        """

    companion object {

        private val declarationBuilderFactory =
            DeclarationBuilderFactory(ScriptGenerationDeclarationBuilderFactory::class.java)
    }
}

abstract class VerificationCapableScriptGenerator<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    S : DeclarationState,
    E : CheckComposingScriptGenerationEnvironment<E>,
    >(
    actionFactory: AF,
    checkFactory: CF,
    declarationStateFactory: DeclarationStateFactory<S>,
    environmentFactory: ScriptGenerationEnvironmentFactory<E>,
    private val actionGeneratorFactory: AGF,
    private val queryQueryFactory: QF,
    private val verifyQueryFactory: QF
) : ScriptGenerator<AF, CF, QF, AGF, S, E>(actionFactory, checkFactory, declarationStateFactory, environmentFactory) {

    @Suppress("UNCHECKED_CAST")
    override fun generateNonVerifiableScenario(
        given: String, whenever: String, scenario: BuildableScenario<AF>
    ): String = when (scenario) {
        is OracleScenario<*, *, *> -> {
            val oracleScenario = scenario as OracleScenario<AF, QF, AGF>
            val givenGenerate = generateGenerateScript(oracleScenario.givenActionGenerations(actionGeneratorFactory))
            val whenGenerate = generateGenerateScript(oracleScenario.whenActionGenerations(actionGeneratorFactory))
            val verify = generateVerifyScript(oracleScenario)
            generateOracleScenarioScript(given, whenever, givenGenerate, whenGenerate, verify)
        }

        is ScenarioWithQueries<*, *> -> {
            val query = generateQueryScript(scenario as ScenarioWithQueries<AF, QF>)
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

    fun generateVerifyScriptFromQueries(scenarioQueries: ScenarioQueries) = if (scenarioQueries.isEmpty()) {
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

    fun generateQueryScriptFromQueries(scenarioQueries: ScenarioQueries) = if (scenarioQueries.isEmpty()) {
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
