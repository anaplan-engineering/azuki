package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.declaration.DeclarationStateBuilder
import com.anaplan.engineering.azuki.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action.AbstactWorldAction
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.action.AbstractWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check.AbstractWorldCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.abstract.check.AbstractWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState
import org.slf4j.LoggerFactory

class AbstractWorldSystemFactory :
    VerifiableSystemFactory<IntraWorldActionFactory<AbstactWorldAction>, IntraWorldCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, AbstractWorldSystem> {

    override fun create(systemDefinition: SystemDefinition): AbstractWorldSystem =
        AbstractWorldSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toAbstractWorldAction),
            systemDefinition.checks.map(toAbstractWorldCheck),
        )

    override val actionFactory = AbstractWorldActionFactory
    override val checkFactory = AbstractWorldCheckFactory

    companion object {
        private val toAbstractWorldAction: (Action) -> AbstactWorldAction = {
            it as? AbstactWorldAction
                ?: throw IllegalArgumentException("Invalid action: $it")
        }

        private val toAbstractWorldCheck: (Check) -> AbstractWorldCheck = {
            it as? AbstractWorldCheck ?: throw IllegalArgumentException("Invalid check: $it")
        }
    }
}

class AbstractWorldSystem(
    private val declarableActions: List<DeclarableAction<IntraWorldDeclarationState>>,
    private val commands: List<AbstactWorldAction>,
    private val checks: List<AbstractWorldCheck>,
) : VerifiableSystem<IntraWorldActionFactory<AbstactWorldAction>, IntraWorldCheckFactory> {

    private fun build(): AbstractWorldAnimation {
        val declarations = DeclarationStateBuilder(::IntraWorldDeclarationState).build(declarableActions)
        // The declaration builder is not particularly useful in this example so we choose not to consume it!
        val builder = AbstractWorldAnimationBuilder(declarations)
        val animation = builder.build()
        commands.forEach { it.act(animation) }
        return animation
    }

    private fun runAllChecks(animation: AbstractWorldAnimation) =
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
