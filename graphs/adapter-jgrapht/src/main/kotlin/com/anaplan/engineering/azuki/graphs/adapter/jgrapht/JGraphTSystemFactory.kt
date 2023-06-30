package com.anaplan.engineering.azuki.graphs.adapter.jgrapht

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.api.GraphCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.graphs.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action.JGraphTAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action.JGraphTActionFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.action.toJGraphTAction
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check.JGraphTCheck
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check.JGraphTCheckFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.check.toJGraphTCheck
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration.JGraphTDeclarationBuilder
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.declaration.JGraphTDeclarationBuilderFactory
import com.anaplan.engineering.azuki.graphs.adapter.jgrapht.execution.ExecutionEnvironment
import org.slf4j.LoggerFactory

class JGraphTSystemFactory : SystemFactory<
    GraphActionFactory,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults
    > {
    override fun create(systemDefinition: SystemDefinition): System<GraphActionFactory, GraphCheckFactory> {
        return JGraphTSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.actions.map(toJGraphTAction),
            systemDefinition.checks.map(toJGraphTCheck),
        )
    }

    override val actionFactory = JGraphTActionFactory()
    override val checkFactory = JGraphTCheckFactory()
    override val queryFactory = NoQueryFactory
    override val actionGeneratorFactory = NoActionGeneratorFactory

}

data class JGraphTSystem(
    val declarableActions: List<DeclarableAction>,
    val buildActions: List<JGraphTAction>,
    val checks: List<JGraphTCheck>
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
        val declarationBuilders = DeclarationBuilder(declarableActions).build().map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(env) }
        buildActions.forEach { it.act(env) }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, JGraphTDeclarationBuilder<D>>(declaration)


    override fun generateReport(name: String) = throw UnsupportedOperationException()

    override fun query() = throw UnsupportedOperationException()

    override fun generateActions() = throw UnsupportedOperationException()

    companion object {
        private val declarationBuilderFactory = DeclarationBuilderFactory(JGraphTDeclarationBuilderFactory::class.java)

        private val Log = LoggerFactory.getLogger(JGraphTSystemFactory::class.java)
    }
}


