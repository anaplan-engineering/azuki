package com.anaplan.engineering.vdmanimation.api

interface VdmSpecification {
    val files: List<VdmFile>
}

data class VdmFile(
        val name: String,
        val text: String
)
