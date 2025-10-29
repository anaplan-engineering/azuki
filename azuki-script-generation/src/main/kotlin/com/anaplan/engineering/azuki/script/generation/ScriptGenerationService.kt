package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.*
import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.declaration.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The main endpoint for script generation tasks.
 */
class ScriptGenerationService<
    // mandatory parameters
    out AF : ActionFactory,
    out CF : CheckFactory,
    DS : DeclarationState,
    // optional parameters (these require calling 'withXYZFactory')
    E : ScriptGenerationEnvironment,
    out QF : QueryFactory,
    out AGF : ActionGeneratorFactory,
    > private constructor(
    // mandatory parameters
    internal val actionFactory: AF,
    internal val checkFactory: CF,
    internal val declarationStateFactory: DeclarationStateFactory<DS>,
    // optional parameters
    internal val environmentFactory: ScriptGenerationEnvironmentFactory<E>,
    internal val queryFactory: QF,
    internal val actionGeneratorFactory: AGF,
) {
    /**
     * Adds an environment factory to this service, changing the type of accepted scenarios accordingly.
     */
    fun <N : ScriptGenerationEnvironment> withEnvironmentFactory(new: ScriptGenerationEnvironmentFactory<N>) =
        ScriptGenerationService(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory = new,
            queryFactory,
            actionGeneratorFactory,
        )

    /**
     * Adds a query factory to this service, changing the type of accepted scenarios accordingly.
     */
    fun <N : QueryFactory> withQueryFactory(new: N) = ScriptGenerationService(
        actionFactory,
        checkFactory,
        declarationStateFactory,
        environmentFactory,
        queryFactory = new,
        actionGeneratorFactory,
    )

    /**
     * Adds an action generation factory to this service, changing the type of accepted scenarios accordingly.
     */
    fun <N : ActionGeneratorFactory> withActionGeneratorFactory(new: N) = ScriptGenerationService(
        actionFactory,
        checkFactory,
        declarationStateFactory,
        environmentFactory,
        queryFactory,
        actionGeneratorFactory = new,
    )

    /**
     * Constructs a given block by mixing in declarations from one or more sources.
     */
    fun given(build: GivenBuilder<AF, DS, E>.() -> Unit) = withNewEnvironment {
        Given(it, GivenBuilder(actionFactory, declarationStateFactory, it).apply(build).scriptFragments)
    }

    /**
     * Constructs a given block with explicit string fragments.
     */
    fun given(vararg scriptFragments: String) = given(scriptFragments.toList())

    /**
     * Constructs a given block with explicit string fragments.
     */
    fun given(scriptFragments: List<String>) = withNewEnvironment { Given(it, scriptFragments) }

    private inline fun <T> withNewEnvironment(f: (E) -> T): T = environmentFactory.create().let(f)

    companion object {

        /**
         * Constructs a basic script generator factory with no optional extras included.
         */
        fun <AF : ActionFactory, CF : CheckFactory, DS : DeclarationState> create(
            actionFactory: AF, checkFactory: CF, declarationStateFactory: DeclarationStateFactory<DS>
        ) = ScriptGenerationService(
            actionFactory,
            checkFactory,
            declarationStateFactory,
            environmentFactory = { NoScriptGenerationEnvironment },
            NoQueryFactory,
            NoActionGeneratorFactory,
        )
    }

    inner class Given(internal val environment: E, scriptFragments: List<String>) :
        ScriptBlock("given", scriptFragments) {

        /**
         * Constructs a whenever block by mixing in declarations from one or more sources.
         */
        fun whenever(build: WheneverBuilder<AF, E>.() -> Unit) =
            whenever(WheneverBuilder(actionFactory, environment).apply(build).scriptFragments)

        /**
         * Constructs a whenever block with explicit string fragments.
         */
        fun whenever(vararg scriptFragments: String) = whenever(scriptFragments.toList())

        /**
         * Constructs a whenever block with explicit string fragments.
         */
        fun whenever(scriptFragments: List<String>) = Whenever(this, scriptFragments)
    }

    inner class Whenever(
        internal val given: Given, scriptFragments: List<String>
    ) : ScriptBlock("whenever", scriptFragments) {

        private val environment get() = given.environment

        fun incomplete() = IncompleteScenarioScript(given, whenever = this)

        /**
         * Constructs a then block by mixing in checks from one or more sources.
         */
        fun then(build: ThenBuilder<CF, E>.() -> Unit) =
            then(ThenBuilder(checkFactory, environment).apply(build).scriptFragments)

        /**
         * Constructs a then block with explicit string fragments.
         */
        fun then(vararg scriptFragments: String) = then(scriptFragments.toList())

        /**
         * Constructs a then block with explicit string fragments.
         */
        fun then(scriptFragments: List<String>) = Then(this, environment, scriptFragments)

        /**
         * Constructs a query block by mixing in queries from one or more sources.
         */
        fun query(build: QueryBuilder<QF, E>.() -> Unit) =
            query(QueryBuilder(queryFactory, environment).apply(build).scriptFragments)

        /**
         * Constructs a query block with explicit script fragments.
         */
        fun query(vararg scriptFragments: String) = query(scriptFragments.toList())

        /**
         * Constructs a query block with explicit script fragments.
         */
        fun query(scriptFragments: List<String>) = Query(this, environment, scriptFragments)
    }

    inner class Then(val whenever: Whenever, scriptFragments: List<String>) : ScriptBlock("then", scriptFragments) {

        internal val given get() = whenever.given

        /**
         * Constructs a verifiable scenario script with the given, when, and then blocks previously constructed.
         */
        fun verifiableScenario() = VerifiableScenarioScript(given, whenever, then = this)
    }

    inner class Query(val whenever: Whenever, scriptFragments: List<String>) : ScriptBlock("query", scriptFragments) {

        val given get() = whenever.given

        /**
         * Constructs a verifiable scenario script with the given, when, and then blocks previously constructed.
         */
        fun queryScenario() = QueryScenarioScript(given, whenever, query = this)
    }
}

abstract class StageBuilder<E : ScriptGenerationEnvironment>(val environment: E) {

    abstract val scriptFragments: List<String>

    companion object {

        internal val Log: Logger = LoggerFactory.getLogger(StageBuilder::class.java)
    }
}

class GivenBuilder<out AF : ActionFactory, DS : DeclarationState, E : ScriptGenerationEnvironment>(
    private val actionFactory: AF, private val declarationStateFactory: DeclarationStateFactory<DS>, environment: E
) : StageBuilder<E>(environment) {

    private val declarableActions = mutableListOf<DeclarableAction<DS>>()

    fun fromSystemDefinition(system: SystemDefinition) = fromActions(system.declarations)

    fun fromScenario(scenario: BuildableScenario<in AF>) = fromActions(scenario.declarations(actionFactory))

    fun fromActions(vararg actions: Action) = fromActions(actions.toList())

    fun fromActions(actions: Collection<Action>) =
        refine<_, _, UnsupportedAction>("declarable action", actions, destination = declarableActions)

    override val scriptFragments: List<String>
        get() = DeclarationStateBuilder(declarationStateFactory).build(declarableActions)
            .map { declarationBuilder(it).getDeclarationScript(environment) }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, ScriptGenerationDeclarationBuilder<E, D>>(declaration)

    companion object {

        private val declarationBuilderFactory =
            DeclarationBuilderFactory(ScriptGenerationDeclarationBuilderFactory::class.java)
    }
}

class WheneverBuilder<out AF : ActionFactory, E : ScriptGenerationEnvironment>(val actionFactory: AF, environment: E) :
    StageBuilder<E>(environment) {

    private val commands = mutableListOf<ScriptGenerationAction<E>>()

    fun fromSystemDefinition(system: SystemDefinition) = fromActions(system.commands)

    fun fromScenario(scenario: BuildableScenario<in AF>) = fromActions(scenario.commands(actionFactory))

    fun fromActions(vararg actions: Action) = fromActions(actions.toList())

    fun fromActions(actions: Collection<Action>) =
        refine<_, _, UnsupportedAction>("command action", actions, destination = commands)

    override val scriptFragments get() = commands.map { it.getActionScript(environment) }
}

class ThenBuilder<out CF : CheckFactory, E : ScriptGenerationEnvironment>(
    private val checkFactory: CF, environment: E
) : StageBuilder<E>(environment) {

    private val checks = mutableListOf<ScriptGenerationCheck<E>>()

    /**
     * Whether to use check composition on creating the final check list.
     */
    var compose = true

    /**
     * Populates the script with the checks present in this system definition.
     */
    fun fromSystemDefinition(system: SystemDefinition) = fromChecks(system.checks)

    /**
     * Populates the script with the checks present in this scenario.
     */
    fun fromScenario(scenario: VerifiableScenario<*, in CF>) = fromChecks(scenario.checks(checkFactory))

    /**
     * Populates the script with checks derived from the given answers.
     */
    fun fromAnswers(answers: Collection<Answer<*, in CF>>) =
        fromChecks(answers.flatMap { it.createChecks(checkFactory) })

    /**
     * Populates the script with the given checks.
     */
    fun fromChecks(vararg checks: Check) = fromChecks(checks.toList())

    /**
     * Populates the script with the given checks.
     */
    fun fromChecks(checks: Collection<Check>) {
        refine<_, _, UnsupportedCheck>("check", checks, destination = this.checks)
    }

    override val scriptFragments
        get() = (if (compose) composedChecks else checks).map { it.getCheckScript(environment) }

    private val composedChecks
        get() = checks.map {
            it to (it as? ComposableScriptGenerationCheck<E>)?.registerComposable(environment)
        }.let { composeChecks(environment, it) }
}

class QueryBuilder<out QF : QueryFactory, E : ScriptGenerationEnvironment>(
    private val queryFactory: QF, environment: E
) : StageBuilder<E>(environment) {

    val queries = mutableListOf<ScriptGenerationQuery<*>>()
    val derivedQueries = mutableListOf<ScriptGenerationDerivedQuery<*>>()

    override val scriptFragments: List<String>
        get() = queries.map { it.getQueryScript() } + derivedQueries.map { it.getDerivedQueryScript() }

    /**
     * Populates the script with the queries and derived queries from the given scenario.
     */
    fun fromScenario(scenario: ScenarioWithQueries<*, in QF>) = fromScenarioQueries(scenario.queries(queryFactory))

    /**
     * Populates the script with the queries and derived queries from the given bundle.
     */
    fun fromScenarioQueries(scenarioQueries: ScenarioQueries) {
        fromQueries(scenarioQueries.queries)
        fromDerivedQueries(scenarioQueries.forAllQueries)
    }

    /**
     * Populates the script with the given queries.
     */
    fun fromQueries(vararg queries: Query<*>) = fromQueries(queries.toList())

    /**
     * Populates the script with the given queries.
     */
    fun fromQueries(queries: Collection<Query<*>>) = refine<_, _, UnsupportedQuery<*>>("query", queries, this.queries)

    /**
     * Populates the script with the given derived queries.
     */
    fun fromDerivedQueries(vararg queries: DerivedQuery<*>) = fromDerivedQueries(queries.toList())

    /**
     * Populates the script with the given derived queries.
     */
    fun fromDerivedQueries(queries: Collection<DerivedQuery<*>>) =
        refine<_, _, Unit>("derived query", queries, this.derivedQueries)
}


/**
 * Refines a collection of untyped actions or checks I into a collection of typed actions or checks O.
 * U is the type of unsupported actions or checks, respectively.
 */
private inline fun <reified I, reified O : I, reified U : I> refine(
    type: String, input: Collection<I>, destination: MutableCollection<in O>
) {
    val oldSize = destination.size;
    input.filterIsInstanceTo<O, _>(destination)
    val allItemsAdded = destination.size == oldSize + input.size
    val refineFailures = if (allItemsAdded) emptyList() else {
        input.filterNot { it in destination }.apply { forEach { logRefineFailure<U>(it) } }
    }
    check(refineFailures.isNotEmpty()) { "Some ${type}s are not able to be generated" }
}

private inline fun <reified U> logRefineFailure(failure: Any?) {
    val why = if (failure is U) "unsupported" else "wrong type"
    StageBuilder.Log.error(" * {}: {}", why, failure)
}
