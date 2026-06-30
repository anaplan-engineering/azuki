package com.anaplan.engineering.azuki.rightofway.adapter.implementation

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionGeneratingSystem
import com.anaplan.engineering.azuki.core.system.ActionGeneratingSystemFactory
import com.anaplan.engineering.azuki.core.system.ActionGenerator
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.core.system.MutableSystem
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.core.system.PersistableSystem
import com.anaplan.engineering.azuki.core.system.PersistableSystemFactory
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryableSystem
import com.anaplan.engineering.azuki.core.system.QueryableSystemFactory
import com.anaplan.engineering.azuki.core.system.RunnableDerivedQuery
import com.anaplan.engineering.azuki.core.system.RunnableQuery
import com.anaplan.engineering.azuki.core.system.SystemDefinition
import com.anaplan.engineering.azuki.core.system.SystemIteration
import com.anaplan.engineering.azuki.core.system.VerificationResult
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.declaration.DeclarationStateBuilder
import com.anaplan.engineering.azuki.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.action.SampleAction
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.action.SampleActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.actionGenerator.SampleActionGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.actionGenerator.SampleActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.check.SampleCheck
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.check.SampleCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.declaration.SampleDeclarationBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.declaration.SampleDeclarationBuilderFactory
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.query.SampleQueryFactory
import com.anaplan.engineering.azuki.rightofway.implementation.AirspaceManager
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import kotlin.collections.forEach

class SampleSystemFactory :
    ActionGeneratingSystemFactory<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, NoSystemDefaults, SampleSystem>,
    PersistableSystemFactory<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, NoSystemDefaults, SampleSystem>,
    QueryableSystemFactory<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, NoSystemDefaults, SampleSystem> {

    override fun create(systemDefinition: SystemDefinition) = SampleSystem(systemDefinition.toSampleSystemDefinition())

    override val actionFactory = SampleActionFactory()
    override val actionGeneratorFactory = SampleActionGeneratorFactory()
    override val checkFactory = SampleCheckFactory()
    override val queryFactory = SampleQueryFactory()
}

data class SampleSystemIteration(
    val buildActions: List<SampleAction>,
    val actionGenerators: List<SampleActionGenerator>,
    val checks: List<SampleCheck>,
    val regardlessOfActions: List<List<SampleAction>>,
    // These two are Runnable*Query to allow us to use generic query combinators as well as RightOfWayBehaviours-specific queries
    val queries: List<RunnableQuery<ExecutionEnvironment, RightOfWayCheckFactory, *>>,
    val derivedQueries: List<RunnableDerivedQuery<ExecutionEnvironment, RightOfWayCheckFactory, *>>,
) {
    fun runBuildActions(env: ExecutionEnvironment) = buildActions.forEach { it.act(env) }
    fun runQueries(env: ExecutionEnvironment) = (queries + deriveQueries(env)).map { it.run(env) }
    fun deriveQueries(env: ExecutionEnvironment) = derivedQueries.flatMap { it.derive(env) }
    fun generateActions(env: ExecutionEnvironment) = actionGenerators.flatMap { it.generate(env) }
    fun runAllChecks(env: ExecutionEnvironment) = checks.all {
        try {
            it.check(env)
        } catch (e: LateDetectUnsupportedCheckException) {
            handleLateDetectedUnsupportedCheck(e)
        }
    }

    private fun handleLateDetectedUnsupportedCheck(e: LateDetectUnsupportedCheckException): Boolean {
        Log.info("Skipping late detected unsupported check", e)
        return true
    }

    infix operator fun plus(other: SampleSystemIteration) = SampleSystemIteration(
        buildActions + other.buildActions,
        actionGenerators + other.actionGenerators,
        checks + other.checks,
        regardlessOfActions + other.regardlessOfActions,
        queries + other.queries,
        derivedQueries + other.derivedQueries,
    )

    companion object {

        private val Log: Logger = LoggerFactory.getLogger(SampleSystemIteration::class.java)
    }
}

fun SystemIteration.toSampleSystemIteration() = SampleSystemIteration(
    commands.map { it.toSampleAction() },
    actionGenerators.map { it.toSampleActionGenerator() },
    checks.map { it.toSampleCheck() },
    regardlessOfActions.map { b -> b.map { it.toSampleAction() } },
    queries.map { it.toSampleQuery() },
    forAllQueries.map { it.toSampleDerivedQuery() },
)

data class SampleSystemDefinition(
    val declarableActions: List<DeclarableAction<RightOfWayDeclarationState>>,
    val buildActions: List<SampleAction>,
    val actionGenerators: List<SampleActionGenerator>,
    val checks: List<SampleCheck>,
    val regardlessOfActions: List<List<SampleAction>>,
    // These two are Runnable*Query to allow us to use generic query combinators as well as RightOfWayBehaviours-specific queries.
    val queries: List<RunnableQuery<ExecutionEnvironment, RightOfWayCheckFactory, *>>,
    val derivedQueries: List<RunnableDerivedQuery<ExecutionEnvironment, RightOfWayCheckFactory, *>>,
)

fun SystemDefinition.toSampleSystemDefinition() = SampleSystemDefinition(
    declarations.map(::toDeclarableAction),
    commands.map { it.toSampleAction() },
    actionGenerators.map { it.toSampleActionGenerator() },
    checks.map { it.toSampleCheck() },
    regardlessOfActions.map { b -> b.map { it.toSampleAction() } },
    queries.map { it.toSampleQuery() },
    forAllQueries.map { it.toSampleDerivedQuery() },
)

private fun Action.toSampleAction() = requireNotNull(this as? SampleAction) { "Invalid action: $this" }

private fun ActionGenerator.toSampleActionGenerator() =
    requireNotNull(this as? SampleActionGenerator) { "Invalid action generator: $this" }

private fun Check.toSampleCheck() = requireNotNull(this as? SampleCheck) { "Invalid check: $this" }

@Suppress("UNCHECKED_CAST")
private fun <T> Query<T>.toSampleQuery() =
    requireNotNull(this as? RunnableQuery<ExecutionEnvironment, RightOfWayCheckFactory, T>) { "Invalid query: $this" }

@Suppress("UNCHECKED_CAST")
private fun <T> DerivedQuery<T>.toSampleDerivedQuery() =
    requireNotNull(this as? RunnableDerivedQuery<ExecutionEnvironment, RightOfWayCheckFactory, T>) {
        "Invalid derived query: $this"
    }

// Mutable right of way system handling persistence and is capable of action generation and querying.
class SampleSystem(private val initialDefinition: SampleSystemDefinition) :
    ActionGeneratingSystem<RightOfWayActionFactory, RightOfWayCheckFactory>,
    PersistableSystem<RightOfWayActionFactory, RightOfWayCheckFactory>,
    QueryableSystem<RightOfWayActionFactory, RightOfWayCheckFactory>,
    MutableSystem<RightOfWayActionFactory, RightOfWayCheckFactory> {

    private var currentIteration: SampleSystemIteration? = null

    private fun <T> processIteration(
        check: SampleSystemIteration.() -> Unit = {}, processor: SampleSystemIteration.() -> T
    ) = with(currentIteration ?: initialize(newStore())) {
        check()
        runBuildActions(env)
        processor()
    }

    private var _store: File? = null
    private val store: File get() = checkNotNull(_store) { "store should have been initialised" }
    private var _env: ExecutionEnvironment? = null
    private val env: ExecutionEnvironment get() = checkNotNull(_env) { "environment should have been initialised" }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, SampleDeclarationBuilder<D>>(declaration)

    override fun verify() = processIteration(check = {
        check(queries.isEmpty()) { "Cannot check and query at the same time" }
        check(actionGenerators.isEmpty()) { "Cannot check and generate actions at the same time" }
    }) {
        try {
            val allChecksPass = runAllChecks(env) && regardlessOfActions.all { actions ->
                actions.forEach { it.act(env) }
                runAllChecks(env)
            }
            if (allChecksPass) VerificationResult.Verified() else VerificationResult.Unverified()
        } catch (e: LateDetectUnsupportedActionException) {
            Log.info("Unsupported action", e)
            throw e
        }
    }

    private val objectMapper = jacksonObjectMapper()

    data class PersistableSystemState(val activeAirspaces: List<String>, val store: File)

    override fun verifyAndSerialize(): VerificationResult {
        val result = verify()
        return if (result is VerificationResult.Verified) {
            try {
                val activeAirspaces = env.airspaceManager.activeAirspaces.map { name ->
                    env.airspaceManager.save(name)
                    name
                }
                val file = Files.createTempFile("sample_rightofway", "json").toFile()
                objectMapper.writeValue(file, PersistableSystemState(activeAirspaces, store))
                VerificationResult.VerifiedAndSerialized(file)
            } catch (e: Exception) {
                Log.error("Unable to serialize", e)
                result
            }
        } else {
            result
        }
    }

    override fun deserializeAndVerify(file: File): VerificationResult {
        val systemState = objectMapper.readValue<PersistableSystemState>(file)
        currentIteration = initialize(systemState.store)
        systemState.activeAirspaces.forEach { env.airspaceManager.load(it) }
        return verify()
    }

    override fun generateActions() = processIteration(check = {
        check(queries.isEmpty() && derivedQueries.isEmpty()) { "Cannot generate actions and query at the same time" }
        check(checks.isEmpty()) { "Cannot generate actions and check at the same time" }
    }) {
        generateActions(env)
    }

    override fun query() = processIteration(check = {
        check(checks.isEmpty()) { "Cannot query and check at the same time" }
        check(actionGenerators.isEmpty()) { "Cannot query and generate actions at the same time" }
    }) {
        runQueries(env)
    }

    private fun newStore() = Files.createTempDirectory("RightOfWay").toFile()

    override fun applyIteration(systemIteration: SystemIteration) {
        currentIteration = if (currentIteration == null) {
            initialize(newStore(), systemIteration)
        } else {
            systemIteration.toSampleSystemIteration()
        }
    }

    // This is a bit fiddly as we want to defer any system initialization until a 'command' function is invoked
    private fun initialize(store: File, initialIteration: SystemIteration? = null) = try {
        _store = store
        _env = ExecutionEnvironment(AirspaceManager(store))
        val declarationBuilders =
            declarationStateBuilder.build(initialDefinition.declarableActions).map { declarationBuilder(it) }
        declarationBuilders.forEach { it.declare(env) }

        val definitionIteration = SampleSystemIteration(initialDefinition.buildActions,
            initialDefinition.actionGenerators,
            initialDefinition.checks,
            initialDefinition.regardlessOfActions,
            initialDefinition.queries,
            initialDefinition.derivedQueries)

        initialIteration?.let { definitionIteration + it.toSampleSystemIteration() } ?: definitionIteration
    } catch (e: LateDetectUnsupportedActionException) {
        Log.info("Unsupported action", e)
        throw e
    }

    override fun destroy() {}

    companion object {

        private val Log = LoggerFactory.getLogger(this::class.java)
        private val declarationBuilderFactory = DeclarationBuilderFactory(SampleDeclarationBuilderFactory::class.java)
        private val declarationStateBuilder = DeclarationStateBuilder(::RightOfWayDeclarationState)
    }
}
