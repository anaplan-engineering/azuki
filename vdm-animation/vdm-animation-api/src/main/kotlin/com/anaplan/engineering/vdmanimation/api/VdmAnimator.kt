package com.anaplan.engineering.vdmanimation.api

import java.util.ServiceLoader

const val VDM_ENGINE_PROPERTY = "com.anaplan.engineering.vdmanimation.engine"

enum class VdmEngine {
    OVERTURE,
    VDMJ,
}

fun resolveVdmEngine(): VdmEngine =
    when (System.getProperty(VDM_ENGINE_PROPERTY)?.lowercase()) {
        null, "overture" -> VdmEngine.OVERTURE
        "vdmj" -> VdmEngine.VDMJ
        else -> throw IllegalArgumentException(
            "Unknown VDM engine '${System.getProperty(VDM_ENGINE_PROPERTY)}'. Use 'overture' or 'vdmj'.",
        )
    }

interface VdmAnimator {
    val engine: VdmEngine

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

private val animator: VdmAnimator by lazy {
    val engine = resolveVdmEngine()
    ServiceLoader.load(VdmAnimator::class.java)
        .firstOrNull { it.engine == engine }
        ?: throw IllegalStateException(
            "VDM engine '$engine' selected but no matching ${VdmAnimator::class.java.name} " +
                "provider found via SPI. Set -PvdmEngine=$engine when building " +
                "so that engine's animator and interpreter are on the classpath.",
        )
}

fun animate(animationTarget: AnimationTarget, animationContext: AnimationContext = AnimationContext()): AnimationResult =
    animator.animate(animationTarget, animationContext)

fun getStructure(specification: VdmSpecification): SpecificationStructure =
    animator.getStructure(specification)
