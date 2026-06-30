package com.anaplan.engineering.azuki.rightofway.adapter.kazuki

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action.KazukiAction
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.action.KazukiActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check.KazukiCheck
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.check.KazukiCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.declaration.KazukiDeclarationBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.declaration.KazukiDeclarationBuilderFactory
import org.slf4j.LoggerFactory
import kotlin.collections.forEach

class KazukiSystemFactory :
    VerifiableSystemFactory<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, NoSystemDefaults, KazukiSystem> {
    override fun create(systemDefinition: SystemDefinition): KazukiSystem =
        KazukiSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toKazukiAction),
            systemDefinition.checks.map(toKazukiCheck),
        )

    override val actionFactory = KazukiActionFactory()
    override val checkFactory = KazukiCheckFactory()

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
    private val declarableActions: List<DeclarableAction<RightOfWayDeclarationState>>,
    private val buildActions: List<KazukiAction>,
    private val checks: List<KazukiCheck>,
) : VerifiableSystem<RightOfWayActionFactory, RightOfWayCheckFactory> {

    private fun build(): ExecutionEnvironment {
        val builder = EnvironmentBuilder()
        val declarationBuilders = declarationStateBuilder.build(declarableActions).map { declarationBuilder(it) }
        declarationBuilders.forEach { it.build(builder) }
        val env = builder.build()
        buildActions.forEach { it.act(env) }
        return env
    }

    private fun <D : Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, KazukiDeclarationBuilder<D>>(declaration)

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
        return try {
            val env = build()
            val allChecksPass = runAllChecks(env)
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

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)

        private val declarationBuilderFactory = DeclarationBuilderFactory(KazukiDeclarationBuilderFactory::class.java)

        private val declarationStateBuilder = DeclarationStateBuilder(::RightOfWayDeclarationState)
    }
}
