package com.anaplan.engineering.azuki.vdm

import com.anaplan.engineering.azuki.vdm.animation.AnimationModule
import com.anaplan.engineering.vdmanimation.api.Import
import com.anaplan.engineering.vdmanimation.api.SpecificationStructure

interface SystemContext

object EmptySystemContext: SystemContext

class ModuleBuilder<SC : SystemContext>(
    val specification: SpecificationStructure,
    val systemContext: SC, // for carrying system-specific data where required
    val requiredImports: Set<Import> = emptySet(),
    val topLevelDeclarations: List<VdmDeclaration> = emptyList(),
    val testSteps: List<String> = emptyList(),
    val failingStep: String? = null,
    val setters: Map<String, (String) -> String> = emptyMap(),
    val getters: Map<String, String> = emptyMap(),
) {
    fun extend(
        requiredImports: Set<Import> = setOf(),
        topLevelDeclarations: List<VdmDeclaration> = listOf(),
        testSteps: List<String> = listOf(),
        failingStep: String? = this.failingStep,
        setters: Map<String, (String) -> String> = mapOf(),
        getters: Map<String, String> = mapOf(),
        systemContext: SC? = null
    ): ModuleBuilder<SC> {
        if (this.failingStep != null && this.failingStep != failingStep) {
            throw IllegalStateException("Cannot have more than one failing step")
        }
        return ModuleBuilder<SC>(
            specification = this.specification,
            requiredImports = this.requiredImports + requiredImports,
            topLevelDeclarations = this.topLevelDeclarations + topLevelDeclarations,
            testSteps = this.testSteps + testSteps,
            failingStep = failingStep,
            setters = this.setters + setters,
            getters = this.getters + getters,
            systemContext = systemContext ?: this.systemContext
        )
    }

    fun build() = AnimationModule(requiredImports, topLevelDeclarations, testSteps, failingStep)

}

typealias DefaultModuleBuilder = ModuleBuilder<EmptySystemContext>


