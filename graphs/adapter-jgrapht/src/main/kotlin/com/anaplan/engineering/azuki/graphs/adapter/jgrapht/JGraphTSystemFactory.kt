package com.anaplan.engineering.azuki.graphs.adapter.jgrapht

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

class JGraphTSystemFactory : VerifiableSystemFactory<
    GraphActionFactory<JGraphTAction>,
    GraphCheckFactory,
    NoQueryFactory,
    NoActionGeneratorFactory,
    NoSystemDefaults,
    JGraphTSystem
    > {
    override fun create(systemDefinition: SystemDefinition) =
        JGraphTSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toJGraphTAction),
            systemDefinition.checks.map(toJGraphTCheck),
        )

    override val actionFactory = JGraphTActionFactory()

    override val checkFactory = JGraphTCheckFactory()

}

data class JGraphTSystem(
    val declarableActions: List<DeclarableAction<GraphDeclarationState>>,
    val commands: List<JGraphTAction>,
    val checks: List<JGraphTCheck>
) : VerifiableSystem<GraphActionFactory<JGraphTAction>, GraphCheckFactory> {

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
        commands.forEach { it.act(env) }
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, JGraphTDeclarationBuilder<D>>(declaration)

    companion object {
        private val declarationBuilderFactory = DeclarationBuilderFactory(JGraphTDeclarationBuilderFactory::class.java)

        private val declarationStateBuilder = DeclarationStateBuilder(GraphDeclarationState.Factory)

        private val Log = LoggerFactory.getLogger(JGraphTSystemFactory::class.java)
    }
}


