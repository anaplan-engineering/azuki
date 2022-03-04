package com.anaplan.engineering.azuki.vdm.animation

import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.azuki.vdm.coverage.CoverageRecorder
import com.anaplan.engineering.azuki.vdm.coverage.SpecificationCoverage
import com.anaplan.engineering.azuki.vdm.coverage.VdmCoverage
import com.anaplan.engineering.vdmanimation.api.*

class VdmEacAnimator(private val animationModule: AnimationModule) {

    fun run(): EacAnimationResult {
        val animationFile = createAnimationFile(animationModule)
        // TODO -- would be nice to pretty print this to the file
        val specification = AnimationSpecification(additionalFiles = arrayOf(animationFile))
        val animationResult = animate(AnimationTarget(specification, "Animation", "AnimateCheck"),
            AnimationContext(quiet = false, expectFailure = animationModule.failingStep != null))
        CoverageRecorder.recordJson(animationResult.animationCoverage)
        return EacAnimationResult(success = animationResult.checkResult,
            coverage = SpecificationCoverage(animationResult.animationCoverage))
    }

    private fun createAnimationFile(animationModule: AnimationModule): VdmFile {
        return VdmFile("Animation",
"""-- @Warning(5000) suppress warnings for unused definitions
module Animation
${generateImports(animationModule.imports)}
definitions
operations
    AnimateCheck: () ==> bool
    AnimateCheck() ==
(
(
${generateDefinitions(animationModule.definitions)}
${generateTestSteps(animationModule)}
);
return true
)

end Animation
"""
        )
    }

    private fun generateTestSteps(animationModule: AnimationModule): String {
        val failingSteps = if (animationModule.failingStep == null) {
            emptyList()
        } else {
            listOf(animationModule.failingStep)
        }
        return (animationModule.testSteps + failingSteps).joinToString("\n")
    }

    private fun generateImports(requiredImports: Set<Import>): String {
        val definitionImports =
            requiredImports.filterIsInstance<DefinitionImport>().groupBy { it.module }.map { (module, imports) ->
                "  from $module\n" + imports.groupBy { it.definitionType }.map { (importType, definitions) ->
                    "    ${importType}s\n" + definitions.map { import -> import.definition }.joinToString("\n")
                }.joinToString("\n")
            }
        val allImports = requiredImports.filterIsInstance<AllImport>().map { i -> "  from ${i.module} all" }
        val imports = (definitionImports + allImports).joinToString(",\n")
        return "imports\n$imports"
    }

    private fun generateDefinitions(declarations: List<VdmDeclaration>) =
        declarations.map { declaration ->
            if (declaration.init == null) {
                "dcl ${declaration.name}: ${declaration.type};"
            } else {
                "dcl ${declaration.name}: ${declaration.type} := ${declaration.init};"
            }
        }.joinToString("\n")

}

data class EacAnimationResult(
    val success: Boolean,
    val coverage: VdmCoverage
)
