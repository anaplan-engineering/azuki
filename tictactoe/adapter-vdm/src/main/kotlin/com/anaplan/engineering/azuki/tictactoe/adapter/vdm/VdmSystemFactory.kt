package com.anaplan.engineering.azuki.tictactoe.adapter.vdm

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilderFactory
import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.toDeclarableAction
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.action.VdmActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check.DefaultVdmCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check.VdmCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.check.toDefaultVdmCheck
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration.VdmDeclarationBuilder
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration.VdmDeclarationBuilderFactory
import com.anaplan.engineering.azuki.vdm.*
import com.anaplan.engineering.azuki.vdm.animation.AnimationModule
import com.anaplan.engineering.azuki.vdm.animation.BaseSpecification
import com.anaplan.engineering.azuki.vdm.animation.VdmEacAnimator
import com.anaplan.engineering.vdmanimation.api.SpecificationStructure
import com.anaplan.engineering.vdmanimation.api.VdmAnimationException

class VdmSystemFactory :
    VerifiableSystemFactory<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, NoSystemDefaults, VdmSystem> {

    override fun create(systemDefinition: SystemDefinition): VdmSystem {
        if (systemDefinition.regardlessOfActions.any {
                it.any { a -> a != UnsupportedAction }
            }) {
            throw UnsupportedOperationException("Specification should not support regardless of checks")
        }
        return VdmSystem(
            systemDefinition.declarations.map(toDeclarableAction),
            systemDefinition.commands.map(toDefaultVdmAction),
            systemDefinition.checks.map(toDefaultVdmCheck),
        )
    }

    override val actionFactory = VdmActionFactory()
    override val checkFactory = VdmCheckFactory()

}

data class VdmSystem(
    private val declarableActions: List<DeclarableAction>,
    private val buildActions: List<DefaultVdmAction>,
    private val checks: List<DefaultVdmCheck>
) : VerifiableSystem<TicTacToeActionFactory, TicTacToeCheckFactory> {

    private fun createAnimationModule(specification: SpecificationStructure): AnimationModule {
        val vdmDeclarationBuilders = DeclarationBuilder(declarableActions).build().map { declarationBuilder(it) }
        val moduleBuilder =
            checks.fold(
                buildActions.fold(
                    vdmDeclarationBuilders.fold(DefaultModuleBuilder(specification,
                        EmptySystemContext)) { acc, c -> c.build(acc) }
                ) { acc, c -> c.build(acc) }
            ) { acc, c -> c.build(acc) }
        return moduleBuilder.build()
    }

    private fun <D: Declaration> declarationBuilder(declaration: D) =
        declarationBuilderFactory.createBuilder<D, VdmDeclarationBuilder<D>>(declaration)

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

    companion object {
        private val declarationBuilderFactory = DeclarationBuilderFactory(VdmDeclarationBuilderFactory::class.java)
    }
}
