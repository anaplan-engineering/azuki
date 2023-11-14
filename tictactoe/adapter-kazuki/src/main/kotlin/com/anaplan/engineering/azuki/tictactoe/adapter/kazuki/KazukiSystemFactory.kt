package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.KazukiDeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.declaration.KazukiDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.action.KazukiAction
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.action.KazukiActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check.KazukiCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.check.KazukiCheckFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

class KazukiSystemFactory :
    SystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {
    override fun create(systemDefinition: SystemDefinition): System<TicTacToeActionFactory, TicTacToeCheckFactory> =
        KazukiSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.actions.map(toKazukiAction),
            systemDefinition.checks.map(toKazukiCheck),
        )

    override val actionFactory = KazukiActionFactory()
    override val checkFactory = KazukiCheckFactory()
    override val queryFactory = NoQueryFactory
    override val actionGeneratorFactory = NoActionGeneratorFactory


    companion object {
        private val toKazukiAction: (Action) -> KazukiAction = {
            it as? KazukiAction ?: throw IllegalArgumentException("Invalid action: $it")
        }

        private val toKazukiCheck: (Check) -> KazukiCheck = {
            it as? KazukiCheck ?: throw IllegalArgumentException("Invalid check: $it")
        }
    }
}

class KazukiSystem(
    private val declarableActions: List<DeclarableAction>,
    private val buildActions: List<KazukiAction>,
    private val checks: List<KazukiCheck>,
) : System<TicTacToeActionFactory, TicTacToeCheckFactory> {

    override val supportedActions: Set<System.SystemAction> =
        if (checks.isNotEmpty()) {
            setOf(System.SystemAction.Verify)
        } else {
            setOf()
        }

    private fun build(builder: AnimationBuilder) {
        val declarationBuilders =
            DeclarationBuilder(declarableActions).build().map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(builder) }
        buildActions.forEach { it.act(builder) }
    }

    private fun <D: Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, KazukiDeclarationBuilder<D>>(declaration)

    private fun runAllChecks(builder: AnimationBuilder) =
        checks.fold(true) { l, r ->
            l && try {
                r.check(builder)
            } catch (e: LateDetectUnsupportedCheckException) {
                handleLateDetectedUnsupportedCheck(e)
            }
        }

    private fun handleLateDetectedUnsupportedCheck(e: LateDetectUnsupportedCheckException): Boolean {
        Log.info("Skipping late detected unsupported check", e)
        return true
    }

    override fun verify(): VerificationResult {
        val builder = AnimationBuilder()
        return try {
            build(builder)
            val allChecksPass = runAllChecks(builder)
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

        private val declarationBuilderFactory = DeclarationBuilderFactory(KazukiDeclarationBuilderFactory::class.java)
    }
}
