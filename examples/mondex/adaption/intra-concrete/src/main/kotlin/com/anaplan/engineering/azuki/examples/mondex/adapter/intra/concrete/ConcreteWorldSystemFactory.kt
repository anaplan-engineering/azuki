package com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.DeclarableAction
import com.anaplan.engineering.azuki.declaration.DeclarationStateBuilder
import com.anaplan.engineering.azuki.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action.ConcreteWorldAction
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.action.ConcreteWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.check.ConcreteWorldCheck
import com.anaplan.engineering.azuki.examples.mondex.adapter.intra.concrete.check.ConcreteWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldActionFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.api.IntraWorldCheckFactory
import com.anaplan.engineering.azuki.examples.mondex.adaption.intra.declaration.IntraWorldDeclarationState
import org.slf4j.LoggerFactory

class ConcreteWorldSystemFactory :
    VerifiableSystemFactory<IntraWorldActionFactory<ConcreteWorldAction>, IntraWorldCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, ConcreteWorldSystem> {

    override fun create(systemDefinition: SystemDefinition): ConcreteWorldSystem =
        ConcreteWorldSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toConcreteWorldAction),
            systemDefinition.checks.map(toConcreteWorldCheck),
        )

    override val actionFactory = ConcreteWorldActionFactory
    override val checkFactory = ConcreteWorldCheckFactory

    companion object {
        private val toConcreteWorldAction: (Action) -> ConcreteWorldAction = {
            it as? ConcreteWorldAction
                ?: throw IllegalArgumentException("Invalid action: $it")
        }

        private val toConcreteWorldCheck: (Check) -> ConcreteWorldCheck = {
            it as? ConcreteWorldCheck ?: throw IllegalArgumentException("Invalid check: $it")
        }
    }
}

class ConcreteWorldSystem(
    private val declarableActions: List<DeclarableAction<IntraWorldDeclarationState>>,
    private val commands: List<ConcreteWorldAction>,
    private val checks: List<ConcreteWorldCheck>,
) : VerifiableSystem<IntraWorldActionFactory<ConcreteWorldAction>, IntraWorldCheckFactory> {

    private fun build(): ConcreteWorldAnimation {
        val declarations = DeclarationStateBuilder(::IntraWorldDeclarationState).build(declarableActions)
        // The declaration builder is not particularly useful in this example so we choose not to consume it!
        val builder = ConcreteWorldAnimationBuilder(declarations)
        val animation = builder.build()
        commands.forEach { it.act(animation) }
        return animation
    }

    private fun runAllChecks(animation: ConcreteWorldAnimation) =
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
