package com.anaplan.engineering.azuki.graphs.adapter.jung

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.core.system.System
import com.anaplan.engineering.azuki.declaration.*
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

class JungSystemFactory : SystemFactory<
    GraphActionFactory,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults
    > {
    override fun create(systemDefinition: SystemDefinition): System<GraphActionFactory, GraphCheckFactory> {
        return JungSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.actions.map(toJungAction),
            systemDefinition.checks.map(toJungCheck),
        )
    }

    override val actionFactory = JungActionFactory()
    override val checkFactory = JungCheckFactory()
    override val queryFactory = NoQueryFactory
    override val actionGeneratorFactory = NoActionGeneratorFactory

}

data class JungSystem(
    val declarableActions: List<DeclarableAction<GraphDeclarationState>>,
    val buildActions: List<JungAction>,
    val checks: List<JungCheck>
) : System<GraphActionFactory, GraphCheckFactory> {

    override val supportedActions = if (checks.isNotEmpty()) {
        setOf(System.SystemAction.Verify)
    } else {
        setOf()
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


    override fun generateReport(name: String) = throw UnsupportedOperationException()

    override fun query() = throw UnsupportedOperationException()

    override fun generateActions() = throw UnsupportedOperationException()

    companion object {
        private val declarationBuilderFactory = DeclarationBuilderFactory(JungDeclarationBuilderFactory::class.java)

        private val declarationStateBuilder = DeclarationStateBuilder(GraphDeclarationState.Factory)

        private val Log = LoggerFactory.getLogger(JungSystemFactory::class.java)
    }
}
