package com.anaplan.engineering.azuki.tictactoe.adapter.dafny

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.action.DafnyAction
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.action.DafnyActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.action.toDafnyAction
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check.DafnyCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check.DafnyCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.check.toDafnyCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.declaration.DafnyDeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.declaration.DafnyDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.dafny.execution.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.toDeclarableAction
import org.slf4j.LoggerFactory

class DafnySystemFactory :
    VerifiableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, DafnySystem> {

    override fun create(systemDefinition: SystemDefinition): DafnySystem {
        if (systemDefinition.regardlessOfActions.any {
                it.any { a -> a != UnsupportedAction }
            }) {
            throw UnsupportedOperationException("Specification should not support regardless of checks")
        }
        return DafnySystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.commands.map(toDafnyAction),
            systemDefinition.checks.map(toDafnyCheck),
        )
    }

    override val actionFactory = DafnyActionFactory()
    override val checkFactory = DafnyCheckFactory()

}

data class DafnySystem(
    private val declarableActions: List<DeclarableAction>,
    private val buildActions: List<DafnyAction>,
    private val checks: List<DafnyCheck>
) : VerifiableSystem<TicTacToeActionFactory, TicTacToeCheckFactory> {

    private fun build(env: ExecutionEnvironment) {
        val declarationBuilders =
            DeclarationBuilder(declarableActions).build().map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, DafnyDeclarationBuilder<D>>(declaration)

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
            // TODO: regardlessOf checks?
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

    companion object {
        private val declarationBuilderFactory = DeclarationBuilderFactory(DafnyDeclarationBuilderFactory::class.java)
        private val Log = LoggerFactory.getLogger(DafnySystemFactory::class.java)
    }
}
