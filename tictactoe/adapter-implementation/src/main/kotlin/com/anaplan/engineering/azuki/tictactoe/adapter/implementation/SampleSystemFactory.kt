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
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import kotlin.UnsupportedOperationException

class SampleSystemFactory :
    SystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {
    override fun create(systemDefinition: SystemDefinition): System<TicTacToeActionFactory, TicTacToeCheckFactory> =
        SampleSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.actions.map(toSampleAction),
            systemDefinition.checks.map(toSampleCheck),
            systemDefinition.regardlessOfActions.map { it.map(toSampleAction) },
        )

    override val actionFactory = SampleActionFactory()
    override val checkFactory = SampleCheckFactory()
    override val queryFactory = NoQueryFactory
    override val actionGeneratorFactory = NoActionGeneratorFactory


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
) : System<TicTacToeActionFactory, TicTacToeCheckFactory> {

    private val store = Files.createTempDirectory("XO").toFile()

    override val supportedActions: Set<System.SystemAction> =
        if (checks.isNotEmpty()) {
            setOf(System.SystemAction.Verify)
        } else {
            setOf()
        }

    private fun build(env: ExecutionEnvironment) {
        val declarationBuilders =
            DeclarationBuilder(declarableActions).build().map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

    private fun <D: Declaration> declarationBuilder(declaration: D) =
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

    override fun verify(): VerificationResult {
        val env = ExecutionEnvironment(GameManager(store))
        return try {
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
    }

    override fun query(): List<Answer<*, TicTacToeCheckFactory>> = throw UnsupportedOperationException()

    override fun generateActions(): List<(TicTacToeActionFactory) -> Action> = throw UnsupportedOperationException()

    override fun generateReport(name: String): File = throw UnsupportedOperationException()

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)

        private val declarationBuilderFactory = DeclarationBuilderFactory(SampleDeclarationBuilderFactory::class.java)
    }
}
