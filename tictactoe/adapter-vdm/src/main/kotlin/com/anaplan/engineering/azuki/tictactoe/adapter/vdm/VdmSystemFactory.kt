package com.anaplan.engineering.azuki.tictactoe.adapter.vdm

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.action.VdmActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check.VdmCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration.createVdmDeclarationBuilder
import com.anaplan.engineering.azuki.vdm.*
import com.anaplan.engineering.azuki.vdm.animation.AnimationModule
import com.anaplan.engineering.azuki.vdm.animation.BaseSpecification
import com.anaplan.engineering.azuki.vdm.animation.VdmEacAnimator
import com.anaplan.engineering.vdmanimation.api.SpecificationStructure
import com.anaplan.engineering.vdmanimation.api.VdmAnimationException

class VdmSystemFactory :
    SystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults> {

    override fun create(systemDefinition: SystemDefinition): VdmSystem {
        if (systemDefinition.regardlessOfActions.any {
                it.any { a -> a != UnsupportedAction }
            }) {
            throw UnsupportedOperationException("Specification should not support regardless of checks")
        }
        return VdmSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.actions.map(toDefaultVdmAction),
            systemDefinition.checks.map(toDefaultVdmCheck),
        )
    }

    override val actionFactory = VdmActionFactory()
    override val checkFactory = VdmCheckFactory()

    override val queryFactory
        get() = throw UnsupportedOperationException("VDM does not support querying")

    override val actionGeneratorFactory
        get() = throw UnsupportedOperationException("VDM does not support action generation")

}

data class VdmSystem(
    val declarableActions: List<DeclarableAction>,
    val buildActions: List<DefaultVdmAction>,
    val checks: List<DefaultVdmCheck>
) : System<TicTacToeActionFactory, TicTacToeCheckFactory> {

    override val supportedActions = if (checks.isNotEmpty()) {
        setOf(System.SystemAction.Verify)
    } else {
        setOf()
    }

    private fun createAnimationModule(specification: SpecificationStructure): AnimationModule {
        val declarations = DeclarationBuilder(declarableActions).build()
        val vdmDeclarationBuilders = declarations.map { createVdmDeclarationBuilder(it) }
        val moduleBuilder =
            checks.fold(
                buildActions.fold(
                    vdmDeclarationBuilders.fold(DefaultModuleBuilder(specification,
                        EmptySystemContext)) { acc, c -> c.build(acc) }
                ) { acc, c -> c.build(acc) }
            ) { acc, c -> c.build(acc) }
        return moduleBuilder.build()
    }

    override fun verify() =
        try {
            val animationModule = createAnimationModule(BaseSpecification.structure)
            val result = VdmEacAnimator(animationModule).run()
            if (result.success) {
                VerificationResult.Verified()
            } else {
                VerificationResult.Unverified()
            }
        } catch (e: VdmAnimationException) {
            VerificationResult.SystemInvalid(e)
        }

    override fun generateReport(name: String) = throw UnsupportedOperationException()

    override fun query() = throw UnsupportedOperationException()

    override fun generateActions() = throw UnsupportedOperationException()
}
