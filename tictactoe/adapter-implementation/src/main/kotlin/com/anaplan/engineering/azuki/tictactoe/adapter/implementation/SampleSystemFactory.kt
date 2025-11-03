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
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.query.SampleDerivedQuery
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.query.SampleQuery
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

class SampleSystemFactory :
    ActionGeneratingSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, NoSystemDefaults, SampleSystem>,
    PersistableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, NoSystemDefaults, SampleSystem>,
    QueryableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, NoSystemDefaults, SampleSystem> {

    override fun create(systemDefinition: SystemDefinition) = with(systemDefinition) {
        SampleSystem(
            declarations.map(::toDeclarableAction),
            commands.map(::toSampleAction),
            actionGenerators.map(::toSampleActionGenerator),
            checks.map(::toSampleCheck),
            regardlessOfActions.map { it.map(::toSampleAction) },
            queries.map(::toSampleQuery),
            forAllQueries.map(::toSampleDerivedQuery),
        )
    }

    override val actionFactory = SampleActionFactory()
    override val actionGeneratorFactory = SampleActionGeneratorFactory()
    override val checkFactory = SampleCheckFactory()
    override val queryFactory = SampleQueryFactory()

    companion object {

        private fun toSampleAction(it: Action) = requireNotNull(it as? SampleAction) { "Invalid action: $it" }

        private fun toSampleActionGenerator(it: ActionGenerator) =
            requireNotNull(it as? SampleActionGenerator) { "Invalid action generator: $it" }

        private fun toSampleCheck(it: Check) = requireNotNull(it as? SampleCheck) { "Invalid check: $it" }

        private fun toSampleQuery(it: Query<*>) = requireNotNull(it as? SampleQuery<*>) { "Invalid query: $it" }

        private fun toSampleDerivedQuery(it: DerivedQuery<*>) =
            requireNotNull(it as? SampleDerivedQuery<*, *>) { "Invalid derived query: $it" }
    }
}

class SampleSystem(
    private val declarableActions: List<DeclarableAction<TicTacToeDeclarationState>>,
    private val buildActions: List<SampleAction>,
    private val actionGenerators: List<SampleActionGenerator>,
    private val checks: List<SampleCheck>,
    private val regardlessOfActions: List<List<SampleAction>>,
    private val queries: List<SampleQuery<*>>,
    private val derivedQueries: List<SampleDerivedQuery<*, *>>,
) : ActionGeneratingSystem<TicTacToeActionFactory, TicTacToeCheckFactory>,
    PersistableSystem<TicTacToeActionFactory, TicTacToeCheckFactory>,
    QueryableSystem<TicTacToeActionFactory, TicTacToeCheckFactory> {

    private fun build(env: ExecutionEnvironment) {
        val declarationBuilders = declarationStateBuilder.build(declarableActions).map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, SampleDeclarationBuilder<D>>(declaration)

    private fun runAllChecks(env: ExecutionEnvironment) = checks.fold(true) { l, r ->
        l && try {
            r.check(env)
        } catch (e: LateDetectUnsupportedCheckException) {
            handleLateDetectedUnsupportedCheck(e)
        }
    }

    private fun handleLateDetectedUnsupportedCheck(e: LateDetectUnsupportedCheckException): Boolean {
        Log.info("Skipping late detected unsupported check", e)
        return true
    }

    override fun verify(): VerificationResult = verify(newEnvironment())

    private fun verify(env: ExecutionEnvironment) = try {
        build(env)
        val allChecksPass = runAllChecks(env) && regardlessOfActions.all { actions ->
            actions.forEach { it.act(env) }
            runAllChecks(env)
        }
        if (allChecksPass) {
            VerificationResult.Verified()
        } else {
            VerificationResult.Unverified()
        }
    } catch (e: LateDetectUnsupportedActionException) {
        Log.info("Unsupported action", e)
        throw e
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)

        private val declarationBuilderFactory = DeclarationBuilderFactory(SampleDeclarationBuilderFactory::class.java)

        private val declarationStateBuilder = DeclarationStateBuilder(::TicTacToeDeclarationState)
    }

    private val objectMapper = jacksonObjectMapper()

    data class PersistableSystemState(
        val activeGames: List<String>, val store: File
    )

    override fun verifyAndSerialize(): VerificationResult {
        val store = newStore()
        val env = ExecutionEnvironment(GameManager(store))
        val result = verify(env)
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
        val gameManager = GameManager(systemState.store)
        systemState.activeGames.forEach {
            gameManager.load(it)
        }
        return verify(ExecutionEnvironment(gameManager))
    }

    override fun generateActions(): List<(TicTacToeActionFactory) -> Action> {
        check(queries.isEmpty() && derivedQueries.isEmpty()) { "Cannot generate actions and query at the same time" }
        check(checks.isEmpty()) { "Cannot generate actions and check at the same time" }

        return withBuiltEnvironment { env -> actionGenerators.flatMap { it.generate(env) } }
    }

    override fun query(): List<Answer<*, TicTacToeCheckFactory>> {
        check(checks.isEmpty()) { "Cannot query and check at the same time" }
        check(actionGenerators.isEmpty()) { "Cannot query and generate actions at the same time" }

        return withBuiltEnvironment { env ->
            val allQueries = queries + derivedQueries.flatMap { it.derive(env) }
            allQueries.map { it.run(env) }
        }
    }

    private fun <T> withBuiltEnvironment(fn: (ExecutionEnvironment) -> T): T = with(newEnvironment()) {
        build(this)
        fn(this)
    }

    private fun newEnvironment() = ExecutionEnvironment(GameManager(newStore()))
    private fun newStore() = Files.createTempDirectory("XO").toFile()
}
