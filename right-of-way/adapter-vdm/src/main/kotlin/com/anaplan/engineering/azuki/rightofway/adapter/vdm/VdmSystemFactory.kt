package com.anaplan.engineering.azuki.rightofway.adapter.vdm

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.declaration.*
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayActionGeneratorFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.RightOfWayDeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.action.VdmActionFactory
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.check.DefaultVdmCheck
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.check.VdmCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.check.toDefaultVdmCheck
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.declaration.VdmDeclarationBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.vdm.declaration.VdmDeclarationBuilderFactory
import com.anaplan.engineering.azuki.vdm.*
import com.anaplan.engineering.azuki.vdm.animation.AnimationModule
import com.anaplan.engineering.azuki.vdm.animation.BaseSpecification
import com.anaplan.engineering.azuki.vdm.animation.VdmEacAnimator
import com.anaplan.engineering.vdmanimation.api.SpecificationStructure
import com.anaplan.engineering.vdmanimation.api.VdmAnimationException

class VdmSystemFactory :
    VerifiableSystemFactory<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory, NoSystemDefaults, VdmSystem> {

    override fun create(systemDefinition: SystemDefinition): VdmSystem {
        if (systemDefinition.regardlessOfActions.any {
                it.any { a -> a != UnsupportedAction }
            }) {
            throw UnsupportedOperationException("Specification should not support regardless of checks")
        }
        return VdmSystem(
            systemDefinition.declarations.map(::toDeclarableAction),
            systemDefinition.commands.map(toDefaultVdmAction),
            systemDefinition.checks.map(toDefaultVdmCheck),
        )
    }

    override val actionFactory = VdmActionFactory()
    override val checkFactory = VdmCheckFactory()

}

data class VdmSystem(
    private val declarableActions: List<DeclarableAction<RightOfWayDeclarationState>>,
    private val buildActions: List<DefaultVdmAction>,
    private val checks: List<DefaultVdmCheck>
) : VerifiableSystem<RightOfWayActionFactory, RightOfWayCheckFactory> {

    private fun createAnimationModule(specification: SpecificationStructure): AnimationModule {
        val vdmDeclarationBuilders = declarationStateBuilder.build(declarableActions).map { declarationBuilder(it) }
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

        private val declarationStateBuilder = DeclarationStateBuilder(::RightOfWayDeclarationState)
    }
}
