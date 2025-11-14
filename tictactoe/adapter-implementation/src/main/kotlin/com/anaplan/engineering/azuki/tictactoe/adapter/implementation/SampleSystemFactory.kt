package com.anaplan.engineering.azuki.tictactoe.adapter.implementation

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action.SampleAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action.SampleActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.actionGenerator.SampleActionGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.actionGenerator.SampleActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.SampleCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.SampleCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.SampleDeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.SampleDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.query.SampleQueryFactory
import com.anaplan.engineering.azuki.tictactoe.implementation.GameManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger

class SampleSystemFactory :
    ActionGeneratingSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, NoSystemDefaults, SampleSystem>,
    PersistableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, NoSystemDefaults, SampleSystem>,
    QueryableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, NoSystemDefaults, SampleSystem> {

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
    // These two are Runnable*Query to allow us to use generic query combinators as well as TicTacToe-specific queries
    val queries: List<RunnableQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>>,
    val derivedQueries: List<RunnableDerivedQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>>,
) {
    fun runBuildActions(env: ExecutionEnvironment) {
        buildActions.forEach { it.act(env) }
    }

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
    commands.map(::toSampleAction),
    actionGenerators.map(::toSampleActionGenerator),
    checks.map(::toSampleCheck),
    regardlessOfActions.map { it.map(::toSampleAction) },
    queries.map(::toSampleQuery),
    forAllQueries.map(::toSampleDerivedQuery),
)

data class SampleSystemDefinition(
    val declarableActions: List<DeclarableAction<TicTacToeDeclarationState>>,
    val buildActions: List<SampleAction>,
    val actionGenerators: List<SampleActionGenerator>,
    val checks: List<SampleCheck>,
    val regardlessOfActions: List<List<SampleAction>>,
    // These two are Runnable*Query to allow us to use generic query combinators as well as TicTacToe-specific queries.
    val queries: List<RunnableQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>>,
    val derivedQueries: List<RunnableDerivedQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>>,
)

fun SystemDefinition.toSampleSystemDefinition() = SampleSystemDefinition(
    declarations.map(::toDeclarableAction),
    commands.map(::toSampleAction),
    actionGenerators.map(::toSampleActionGenerator),
    checks.map(::toSampleCheck),
    regardlessOfActions.map { it.map(::toSampleAction) },
    queries.map(::toSampleQuery),
    forAllQueries.map(::toSampleDerivedQuery),
)

private fun toSampleAction(it: Action) = requireNotNull(it as? SampleAction) { "Invalid action: $it" }

private fun toSampleActionGenerator(it: ActionGenerator) =
    requireNotNull(it as? SampleActionGenerator) { "Invalid action generator: $it" }

private fun toSampleCheck(it: Check) = requireNotNull(it as? SampleCheck) { "Invalid check: $it" }

@Suppress("UNCHECKED_CAST")
private fun toSampleQuery(it: Query<*>) =
    requireNotNull(it as? RunnableQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>) { "Invalid query: $it" }

@Suppress("UNCHECKED_CAST")
private fun toSampleDerivedQuery(it: DerivedQuery<*>) =
    requireNotNull(it as? RunnableDerivedQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>) {
        "Invalid derived query: $it"
    }

/**
 * An example of a mutable system for tic-tac-toe.
 *
 * This system handles persistence and is capable of action generation and querying.
 */
class SampleSystem(
    private val initialDefinition: SampleSystemDefinition
) : ActionGeneratingSystem<TicTacToeActionFactory, TicTacToeCheckFactory>,
    PersistableSystem<TicTacToeActionFactory, TicTacToeCheckFactory>,
    QueryableSystem<TicTacToeActionFactory, TicTacToeCheckFactory>,
    MutableSystem<TicTacToeActionFactory, TicTacToeCheckFactory> {

    private var currentIteration: SampleSystemIteration? = null

    private fun <T> processIteration(
        check: SampleSystemIteration.() -> Unit = {}, processor: SampleSystemIteration.() -> T
    ) = with(currentIteration ?: initialize(newStore())) {
        println("<*> new iteration: play orders: ${env.playOrders}")
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
        check(actionGenerators.isNotEmpty()) { "Cannot check and generate actions at the same time" }
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

    data class PersistableSystemState(
        val activeGames: List<String>, val store: File
    )

    override fun verifyAndSerialize(): VerificationResult {
        val result = verify()
        return if (result is VerificationResult.Verified) {
            try {
                val activeGames = env.gameManager.activeGames.map { name ->
                    env.gameManager.save(name)
                    name
                }
                val file = Files.createTempFile("sample", "json").toFile()
                objectMapper.writeValue(file, PersistableSystemState(activeGames, store))
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
        systemState.activeGames.forEach {
            env.gameManager.load(it)
        }
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

    private fun newStore() = Files.createTempDirectory("XO").toFile()

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
        _env = ExecutionEnvironment(GameManager(store))
        val declarationBuilders =
            declarationStateBuilder.build(initialDefinition.declarableActions).map { declarationBuilder(it) }
        declarationBuilders.forEach { it.declare(env) }

        val definitionIteration = SampleSystemIteration(initialDefinition.buildActions,
            initialDefinition.actionGenerators,
            initialDefinition.checks,
            initialDefinition.regardlessOfActions,
            initialDefinition.queries,
            initialDefinition.derivedQueries)

        if (initialIteration == null) {
            definitionIteration
        } else {
            definitionIteration + initialIteration.toSampleSystemIteration()
        }
    } catch (e: LateDetectUnsupportedActionException) {
        Log.info("Unsupported action", e)
        throw e
    }

    override fun destroy() {}

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)

        private val declarationBuilderFactory = DeclarationBuilderFactory(SampleDeclarationBuilderFactory::class.java)
        private val declarationStateBuilder = DeclarationStateBuilder(::TicTacToeDeclarationState)
    }
}
