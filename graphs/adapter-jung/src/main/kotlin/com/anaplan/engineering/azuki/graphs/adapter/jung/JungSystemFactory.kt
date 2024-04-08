package com.anaplan.engineering.azuki.graphs.adapter.jung

import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedActionException
import com.anaplan.engineering.azuki.core.system.LateDetectUnsupportedCheckException
import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.core.system.NoSystemDefaults
import com.anaplan.engineering.azuki.core.system.SystemDefinition
import com.anaplan.engineering.azuki.core.system.VerifiableSystem
import com.anaplan.engineering.azuki.core.system.VerifiableSystemFactory
import com.anaplan.engineering.azuki.core.system.VerificationResult
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.declaration.DeclarationStateBuilder
import com.anaplan.engineering.azuki.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.GraphDeclarationState
import com.anaplan.engineering.azuki.graphs.adapter.jung.action.JungAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.action.JungActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.action.toJungAction
import com.anaplan.engineering.azuki.graphs.adapter.jung.check.JungCheck
import com.anaplan.engineering.azuki.graphs.adapter.jung.check.JungCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.check.toJungCheck
import com.anaplan.engineering.azuki.graphs.adapter.jung.declaration.JungDeclarationBuilder
import com.anaplan.engineering.azuki.graphs.adapter.jung.declaration.JungDeclarationBuilderFactory
import com.anaplan.engineering.azuki.graphs.adapter.jung.execution.ExecutionEnvironment
import org.slf4j.LoggerFactory

class JungSystemFactory : VerifiableSystemFactory<
    GraphActionFactory<JungAction>,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults,
    JungSystem
    > {
    override fun create(systemDefinition: SystemDefinition) =
        JungSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toJungAction),
            systemDefinition.checks.map(toJungCheck),
        )

    override val actionFactory = JungActionFactory()
    override val checkFactory = JungCheckFactory()

}

data class JungSystem(
    val declarableActions: List<DeclarableAction<GraphDeclarationState>>,
    val buildActions: List<JungAction>,
    val checks: List<JungCheck>
) : VerifiableSystem<GraphActionFactory<JungAction>, GraphCheckFactory> {

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

    private fun build(env: ExecutionEnvironment) {
        val declarationBuilders = declarationStateBuilder.build(declarableActions).map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, JungDeclarationBuilder<D>>(declaration)


    companion object {
        private val declarationBuilderFactory = DeclarationBuilderFactory(JungDeclarationBuilderFactory::class.java)

        private val declarationStateBuilder = DeclarationStateBuilder(GraphDeclarationState.Factory)

        private val Log = LoggerFactory.getLogger(JungSystemFactory::class.java)
    }
}
