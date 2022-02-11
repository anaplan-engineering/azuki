package com.anaplan.engineering.azuki.vdm

interface VdmDeclarationType

data class VdmDeclaration(
    val name: String,
    val type: VdmDeclarationType,
    val init: String? = null
)

