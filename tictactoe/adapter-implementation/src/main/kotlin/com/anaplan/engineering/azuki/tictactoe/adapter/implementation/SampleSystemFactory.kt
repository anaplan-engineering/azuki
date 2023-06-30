package com.anaplan.engineering.azuki.tictactoe.adapter.implementation

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.core.system.System
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action.SampleAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.action.SampleActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.SampleCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.check.SampleCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.createSampleDeclarationBuilder
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.UnsupportedOperationException

class SampleSystemFactory :
    SystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {
    override fun create(systemDefinition: SystemDefinition): System<TicTacToeActionFactory, TicTacToeCheckFactory> =
        SampleSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.actions.map(toSampleAction),
            systemDefinition.checks.map(toSampleCheck),
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
    val declarableActions: List<DeclarableAction>,
    val buildActions: List<SampleAction>,
    val checks: List<SampleCheck>
) : System<TicTacToeActionFactory, TicTacToeCheckFactory> {

    override val supportedActions: Set<System.SystemAction> =
        if (checks.isNotEmpty()) {
            setOf(System.SystemAction.Verify)
        } else {
            setOf()
        }

    private fun build(env: ExecutionEnvironment) {
        val declarationBuilders =
            DeclarationBuilder(declarableActions).build().map { createSampleDeclarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

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
        val env = ExecutionEnvironment()
        return try {
            build(env)
            if (runAllChecks(env)) {
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
    }
}
