package com.anaplan.engineering.azuki.vdm.animation

import com.anaplan.engineering.vdmanimation.api.Import
import com.anaplan.engineering.azuki.vdm.VdmDeclaration

data class AnimationModule(
    val imports: Set<Import>,
    val definitions: List<VdmDeclaration>,
    val testSteps: List<String>,
    val failingStep: String?
)
