package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.declaration.DeclarationStateBuilder
import com.anaplan.engineering.azuki.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action.WorldAction
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.action.BetweenWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.WorldCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.between.check.BetweenWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState
import org.slf4j.LoggerFactory

class BetweenWorldSystemFactory :
    VerifiableSystemFactory<IntraWorldActionFactory<WorldAction>, IntraWorldCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, BetweenWorldSystem> {

    override fun create(systemDefinition: SystemDefinition) : BetweenWorldSystem =
        BetweenWorldSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toBetweenWorldAction),
            systemDefinition.checks.map(toBetweenWorldCheck),
        )

    override val actionFactory = BetweenWorldActionFactory
    override val checkFactory = BetweenWorldCheckFactory

    companion object {
        private val toBetweenWorldAction: (Action) -> WorldAction = {
            it as? WorldAction
                ?: throw IllegalArgumentException("Invalid actions: $it")
        }

        private val toBetweenWorldCheck: (Check) -> WorldCheck = {
            it as? WorldCheck ?: throw IllegalArgumentException("Invalid check: $it")
        }
    }
}

class BetweenWorldSystem(
    private val declarableActions: List<DeclarableAction<IntraWorldDeclarationState>>,
    private val commands: List<WorldAction>,
    private val checks: List<WorldCheck>,
) : VerifiableSystem<IntraWorldActionFactory<WorldAction>, IntraWorldCheckFactory> {

    private fun build(): BetweenWorldAnimation {
        val declarations = DeclarationStateBuilder(::IntraWorldDeclarationState).build(declarableActions)
        // declaration builder not helpful
        val builder = BetweenWorldAnimationBuilder(declarations)
        val animation = builder.build()
        commands.forEach { it.act(animation) }
        return animation
    }

    private fun runAllChecks(animation: BetweenWorldAnimation) =
        checks.fold(true) { l, r ->
            l && try {
                r.check(animation)
            } catch (e: LateDetectUnsupportedCheckException) {
                Log.info("Skipping late detected unsupported check", e)
                return true
            }
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
    }
}
