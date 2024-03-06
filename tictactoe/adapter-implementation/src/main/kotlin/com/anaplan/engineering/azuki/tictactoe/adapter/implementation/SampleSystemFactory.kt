package com.anaplan.engineering.azuki.tictactoe.adapter.implementation

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.core.system.System
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action.SampleAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action.SampleActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.SampleCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.SampleCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.SampleDeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.SampleDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.implementation.GameManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import kotlin.UnsupportedOperationException
import com.fasterxml.jackson.module.kotlin.readValue

class SampleSystemFactory :
    PersistableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, SampleSystem> {

    override fun create(systemDefinition: SystemDefinition) =
        SampleSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.commands.map(toSampleAction),
            systemDefinition.checks.map(toSampleCheck),
            systemDefinition.regardlessOfActions.map { it.map(toSampleAction) },
        )

    override val actionFactory = SampleActionFactory()
    override val checkFactory = SampleCheckFactory()

    companion object {
        private val toSampleAction: (Action) -> SampleAction = {
            it as? SampleAction ?: throw IllegalArgumentException("Invalid action: $it")
        }

        private val toSampleCheck: (Check) -> SampleCheck = {
            it as? SampleCheck ?: throw IllegalArgumentException("Invalid check: $it")
        }
    }
}

class SampleSystem(
    private val declarableActions: List<DeclarableAction>,
    private val buildActions: List<SampleAction>,
    private val checks: List<SampleCheck>,
    private val regardlessOfActions: List<List<SampleAction>>,
) : PersistableSystem<TicTacToeActionFactory, TicTacToeCheckFactory> {

    private fun build(env: ExecutionEnvironment) {
        val declarationBuilders =
            DeclarationBuilder(declarableActions).build().map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, SampleDeclarationBuilder<D>>(declaration)

    private fun runAllChecks(env: ExecutionEnvironment) =
        checks.fold(true) { l, r ->
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

    override fun verify(): VerificationResult =
        verify(ExecutionEnvironment(GameManager(Files.createTempDirectory("XO").toFile())))

    private fun verify(env: ExecutionEnvironment) =
        try {
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
    }

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule())

    data class PersistableSystemState(
        val activeGames: List<String>,
        val store: File
    )

    override fun verifyAndSerialize(): VerificationResult {
        val store = Files.createTempDirectory("XO").toFile()
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
}
