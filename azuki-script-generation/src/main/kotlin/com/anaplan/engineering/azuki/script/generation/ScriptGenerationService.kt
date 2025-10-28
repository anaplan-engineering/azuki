package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.*
import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
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

    abstract inner class ScriptStage(
        protected val environment: E, header: String, scriptFragments: List<String>
    ) : ScriptBlock(header, scriptFragments)

    inner class Given(environment: E, scriptFragments: List<String>) :
        ScriptStage(environment, "given", scriptFragments) {

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
        fun whenever(scriptFragments: List<String>) = Whenever(this, environment, scriptFragments)
    }

    inner class Whenever(
        val given: Given, environment: E, scriptFragments: List<String>
    ) : ScriptStage(environment, "whenever", scriptFragments) {

        fun incomplete() = IncompleteScenarioScript(given, whenever = this)

        /**
         * Constructs a then block by mixing in declarations from one or more sources.
         */
        fun then(build: ThenBuilder<CF, E>.() -> Unit) =
            then(ThenBuilder(checkFactory, environment).apply(build).scriptFragments)

        /**
         * Constructs a then block with explicit string fragments.
         */
        fun then(vararg scriptFragments: String) = Then(this, environment, scriptFragments.toList())

        /**
         * Constructs a then block with explicit string fragments.
         */
        fun then(scriptFragments: List<String>) = Then(this, environment, scriptFragments)
    }

    inner class Then(
        val whenever: Whenever, environment: E, scriptFragments: List<String>
    ) : ScriptStage(environment, "then", scriptFragments) {

        val given get() = whenever.given

        /**
         * Constructs a verifiable scenario script with the given, when, and then blocks previously constructed.
         */
        fun verifiable() = VerifiableScenarioScript(given, whenever, then = this)
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

    fun fromSystemDefinition(system: SystemDefinition) {
        fromActions(system.declarations)
    }

    fun fromScenario(scenario: BuildableScenario<in AF>) {
        fromActions(scenario.declarations(actionFactory))
    }

    fun fromActions(vararg actions: Action) {
        fromActions(actions.toList())
    }

    fun fromActions(actions: Collection<Action>) {
        declarableActions.addAll(refineActions(actions))
    }

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

    fun fromSystemDefinition(system: SystemDefinition) {
        fromActions(system.commands)
    }

    fun fromScenario(scenario: BuildableScenario<in AF>) {
        fromActions(scenario.commands(actionFactory))
    }

    fun fromActions(vararg actions: Action) {
        fromActions(actions.toList())
    }

    fun fromActions(actions: Collection<Action>) {
        commands.addAll(refineActions(actions))
    }

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
    fun fromAnswers(answers: Collection<Answer<*, in CF>>) = fromChecks(answers.flatMap { it.createChecks(checkFactory) })

    /**
     * Populates the script with the given checks.
     */
    fun fromChecks(vararg checks: Check) = fromChecks(checks.toList())

    /**
     * Populates the script with the given checks.
     */
    fun fromChecks(checks: Collection<Check>) {
        this.checks.addAll(refineChecks(checks))
    }

    override val scriptFragments
        get() = (if (compose) composedChecks else checks).map { it.getCheckScript(environment) }

    private val composedChecks
        get() = checks.map {
            it to (it as? ComposableScriptGenerationCheck<E>)?.registerComposable(environment)
        }.let { composeChecks(environment, it) }
}

private inline fun <reified T : Action> refineActions(actions: Collection<Action>): List<T> {
    if (actions.filterIsInstance<UnsupportedAction>().isNotEmpty()) {
        actions.forEach { StageBuilder.Log.error(" * {}", it) }
        throw IllegalArgumentException("Scriptgen is missing action")
    }
    val filtered = actions.filterIsInstance<T>()
    if (filtered.size != actions.size) {
        actions.filterNot { it in filtered }.forEach { StageBuilder.Log.error(" * {}", it) }
        throw IllegalArgumentException("Some actions were not of the correct type")
    }
    return filtered
}

private inline fun <reified T : Check> refineChecks(checks: Collection<Check>): List<T> {
    if (checks.filterIsInstance<UnsupportedCheck>().isNotEmpty()) {
        checks.forEach { StageBuilder.Log.error(" * {}", it) }
        throw IllegalArgumentException("Scriptgen is missing check")
    }
    val filtered = checks.filterIsInstance<T>()
    if (filtered.size != checks.size) {
        checks.filterNot { it in filtered }.forEach { StageBuilder.Log.error(" * {}", it) }
        throw IllegalArgumentException("Some checks were not of the correct type")
    }
    return filtered
}
