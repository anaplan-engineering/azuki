package com.anaplan.engineering.vdmanimation.api

import java.util.*

interface VdmAnimator {
    fun animate(animationTarget: AnimationTarget, animationContext: AnimationContext): AnimationResult

    fun getStructure(specification: VdmSpecification): SpecificationStructure
}

data class AnimationResult(
        val checkResult: Boolean,
        val animationCoverage: AnimationCoverage
)

data class AnimationTarget(
    val specification: VdmSpecification,
    val module: String,
    val operation: String
)

data class AnimationContext(
        val quiet: Boolean = true,
        val expectFailure: Boolean = true
)

private val animators: List<VdmAnimator> by lazy {
    val loader = ServiceLoader.load(VdmAnimator::class.java)
    loader.iterator().asSequence().toList()
}

// TODO - strategy for choosing impl
fun animate(animationTarget: AnimationTarget, animationContext: AnimationContext = AnimationContext()): AnimationResult {
    return animators.first().animate(animationTarget, animationContext)
}

fun getStructure(specification: VdmSpecification): SpecificationStructure {
    return animators.first().getStructure((specification))
}
