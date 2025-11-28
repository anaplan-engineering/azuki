package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.scenario.ScenarioWithQueries
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.core.system.UnsupportedActionGenerator
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.ValidatableAnswer
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.declaration.DeclarationStateBuilder
import com.anaplan.engineering.azuki.declaration.DeclarationStateFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.collections.plusAssign

/**
 * Builder for a basic script block (which may be a top-level stage of a scenario script, or contained in a composite
 * block)
 */
abstract class BasicScriptBlockBuilder() {

    /**
     * Supplies script fragments to this builder directly.
     * Note that they will appear before any script fragments produced in any other way.
     */
    fun fromFragments(vararg fragments: String) {
        explicitScriptFragments += fragments
    }

    private val explicitScriptFragments = mutableListOf<String>()
    protected abstract val builtScriptElements: List<ScriptElement>
    protected val scriptElements get() = explicitScriptFragments.map(::ScriptStringFragment) + builtScriptElements
}

/**
 * Builder for a basic script block that represents a stage of a scenario script.
 */
abstract class BasicStageBuilder<in S : BuildableScenario<*>, E : ScriptGenerationEnvironment>(val environment: E) :
    BasicScriptBlockBuilder() {

    /**
     * Supplies script fragments from the corresponding parts of the given scenario.
     */
    abstract fun fromScenario(scenario: S)

    companion object {

        internal val Log: Logger = LoggerFactory.getLogger(BasicStageBuilder::class.java)
    }
}

class GivenBuilder<out AF : ActionFactory, DS : DeclarationState, E : ScriptGenerationEnvironment>(
    private val actionFactory: AF, private val declarationStateFactory: DeclarationStateFactory<DS>, environment: E
) : BasicStageBuilder<BuildableScenario<in AF>, E>(environment) {

    fun build(body: GivenBuilder<AF, DS, E>.() -> Unit) = apply(body).scriptElements

    fun fromActions(vararg actions: Action) = fromActions(actions.toList())

    fun fromActions(actions: Collection<Action>) =
        this.declarableActions.addAllWithRefinement<_, _, UnsupportedAction>("declarable action", actions)

    override fun fromScenario(scenario: BuildableScenario<in AF>) = fromActions(scenario.declarations(actionFactory))

    override val builtScriptElements
        get() = DeclarationStateBuilder(declarationStateFactory).build(declarableActions)
            .map { ScriptStringFragment(declarationBuilder(it).getDeclarationScript(environment)) }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, ScriptGenerationDeclarationBuilder<E, D>>(declaration)

    private val declarableActions = mutableListOf<DeclarableAction<DS>>()

    companion object {

        private val declarationBuilderFactory =
            DeclarationBuilderFactory(ScriptGenerationDeclarationBuilderFactory::class.java)
    }
}

class WheneverBuilder<out AF : ActionFactory, E : ScriptGenerationEnvironment>(val actionFactory: AF, environment: E) :
    BasicStageBuilder<BuildableScenario<in AF>, E>(environment) {

    fun build(body: WheneverBuilder<AF, E>.() -> Unit) = apply(body).scriptElements

    /**
     * Populates the script with these commands.
     */
    fun fromActions(vararg actions: Action) = fromActions(actions.toList())

    /**
     * Populates the script with these commands.
     */
    fun fromActions(actions: Collection<Action>) =
        this.commands.addAllWithRefinement<_, _, UnsupportedAction>("command action", actions)

    override fun fromScenario(scenario: BuildableScenario<in AF>) = fromActions(scenario.commands(actionFactory))

    override val builtScriptElements get() = commands.map { ScriptStringFragment(it.getActionScript(environment)) }

    private val commands = mutableListOf<ScriptGenerationAction<E>>()
}

class ThenBuilder<out CF : CheckFactory, E : ScriptGenerationEnvironment>(
    private val checkFactory: CF, environment: E
) : BasicStageBuilder<VerifiableScenario<*, in CF>, E>(environment) {

    fun build(body: ThenBuilder<CF, E>.() -> Unit) = apply(body).scriptElements

    /**
     * Whether to use check composition on creating the final check list.
     */
    var compose = true

    /**
     * Populates the script with checks derived from these answers.
     */
    fun fromAnswers(answers: Collection<Answer<*, in CF>>) =
        fromChecks(answers.flatMap { it.createChecks(checkFactory) })

    /**
     * Populates the script with checks derived from these validatable answers.
     */
    fun fromValidatableAnswers(answers: Collection<ValidatableAnswer<*, in CF>>) =
        fromChecks(answers.flatMap { it.createValidationChecks(checkFactory)})

    /**
     * Populates the script with these checks.
     */
    fun fromChecks(vararg checks: Check) = fromChecks(checks.toList())

    /**
     * Populates the script with these checks.
     */
    fun fromChecks(checks: Collection<Check>) =
        this.checks.addAllWithRefinement<_, _, UnsupportedCheck>("check", checks)

    override fun fromScenario(scenario: VerifiableScenario<*, in CF>) = fromChecks(scenario.checks(checkFactory))

    override val builtScriptElements
        get() = (if (compose) composedChecks else checks).map { ScriptStringFragment(it.getCheckScript(environment)) }

    private val composedChecks
        get() = checks.map {
            it to (it as? ComposableScriptGenerationCheck<E>)?.registerComposable(environment)
        }.let { composeChecks(environment, it) }

    private val checks = mutableListOf<ScriptGenerationCheck<E>>()
}

class QueryBuilder<out QF : QueryFactory, E : ScriptGenerationEnvironment>(
    private val queryFactory: QF, environment: E
) : BasicStageBuilder<ScenarioWithQueries<*, in QF>, E>(environment) {

    fun build(body: QueryBuilder<QF, E>.() -> Unit) = apply(body).scriptElements

    /**
     * Populates the script with the queries and derived queries from the given bundle.
     */
    fun fromScenarioQueries(scenarioQueries: ScenarioQueries) {
        fromQueries(scenarioQueries.queries)
        fromDerivedQueries(scenarioQueries.forAllQueries)
    }

    /**
     * Populates the script with these queries.
     */
    fun fromQueries(vararg queries: Query<*>) = fromQueries(queries.toList())

    /**
     * Populates the script with these queries.
     */
    fun fromQueries(queries: Collection<Query<*>>) =
        this.queries.addAllWithRefinement<_, _, UnsupportedQuery<*>>("query", queries)

    /**
     * Populates the script with these derived queries.
     */
    fun fromDerivedQueries(vararg queries: DerivedQuery<*>) = fromDerivedQueries(queries.toList())

    /**
     * Populates the script with these derived queries.
     */
    fun fromDerivedQueries(queries: Collection<DerivedQuery<*>>) =
        this.derivedQueries.addAllWithRefinement<_, _, Unit>("derived query", queries)

    override fun fromScenario(scenario: ScenarioWithQueries<*, in QF>) =
        fromScenarioQueries(scenario.queries(queryFactory))

    override val builtScriptElements
        get() = queries.map { ScriptStringFragment(it.getQueryScript()) } + derivedQueries.map { ScriptStringFragment(it.getDerivedQueryScript()) }

    private val queries = mutableListOf<ScriptGenerationQuery<*>>()
    private val derivedQueries = mutableListOf<ScriptGenerationDerivedQuery<*>>()
}

abstract class GenerateBuilder<out AF : ActionFactory, out QF : QueryFactory, out AGF : ActionGeneratorFactory>(
    protected val actionGeneratorFactory: AGF
) {

    fun build(body: GenerateBuilder<AF, QF, AGF>.() -> Unit) = apply(body).subBlocks

    /**
     * Adds a new block to the generate list, with the contents provided to the builder.
     */
    fun block(body: GenerateBlockBuilder.() -> Unit) =
        subBlocks.add(ScriptBlock("generate", GenerateBlockBuilder().build(body)))

    /**
     * Adds the generate blocks from this oracle scenario.
     */
    fun blocksFromScenario(scenario: OracleScenario<in AF, in QF, in AGF>) =
        actionGeneratorsFromScenario(scenario).forEach {
            block {
                fromActionGenerators(it)
            }
        }

    protected abstract fun actionGeneratorsFromScenario(scenario: OracleScenario<in AF, in QF, in AGF>): List<List<ActionGenerator>>

    private val subBlocks = mutableListOf<ScriptBlock>()
}

/**
 * Builds an individual block in a list of generate blocks.
 */
class GenerateBlockBuilder() : BasicScriptBlockBuilder() {

    fun build(body: GenerateBlockBuilder.() -> Unit) = apply(body).scriptElements

    /**
     * Populates the script with these action generators.
     */
    fun fromActionGenerators(actionGenerators: List<ActionGenerator>) {
        this.actionGenerators.addAllWithRefinement<_, _, UnsupportedActionGenerator>("action generator",
            actionGenerators)
    }

    private val actionGenerators = mutableListOf<ScriptGenerationActionGenerator>()
    override val builtScriptElements get() = actionGenerators.map { ScriptStringFragment(it.getActionGeneratorScript()) }
}

class GivenGenerateBuilder<out AF : ActionFactory, out QF : QueryFactory, out AGF : ActionGeneratorFactory>(
    actionGeneratorFactory: AGF
) : GenerateBuilder<AF, QF, AGF>(actionGeneratorFactory) {

    override fun actionGeneratorsFromScenario(scenario: OracleScenario<in AF, in QF, in AGF>) =
        scenario.givenActionGenerations(actionGeneratorFactory)
}

class WheneverGenerateBuilder<out AF : ActionFactory, out QF : QueryFactory, out AGF : ActionGeneratorFactory>(
    actionGeneratorFactory: AGF
) : GenerateBuilder<AF, QF, AGF>(actionGeneratorFactory) {

    override fun actionGeneratorsFromScenario(scenario: OracleScenario<in AF, in QF, in AGF>) =
        scenario.whenActionGenerations(actionGeneratorFactory)
}

/**
 * Refines a collection of untyped actions or checks I into a collection of typed actions or checks O.
 * U is the type of unsupported actions or checks, respectively.
 */
private inline fun <reified I, reified O : I, reified U : I> MutableCollection<in O>.addAllWithRefinement(
    type: String, input: Collection<I>
) {
    val oldSize = size;
    input.filterIsInstanceTo<O, _>(this)
    val allItemsAdded = size == oldSize + input.size
    val refineFailures = if (allItemsAdded) emptyList() else {
        input.filterNot { it in this }.apply { forEach { logRefineFailure<U>(it) } }
    }
    check(refineFailures.isEmpty()) { "Some ${type}s are not able to be generated" }
}

private inline fun <reified U> logRefineFailure(failure: Any?) {
    val why = if (failure is U) "unsupported" else "wrong type"
    BasicStageBuilder.Log.error(" * {}: {}", why, failure)
}
