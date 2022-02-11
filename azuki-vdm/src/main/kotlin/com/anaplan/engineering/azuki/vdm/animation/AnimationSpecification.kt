package com.anaplan.engineering.azuki.vdm.animation

import com.anaplan.engineering.vdmanimation.api.SpecificationStructure
import com.anaplan.engineering.vdmanimation.api.VdmFile
import com.anaplan.engineering.vdmanimation.api.VdmSpecification
import com.anaplan.engineering.vdmanimation.api.getStructure

object BaseSpecification : VdmSpecification {

    private val vdmFileRegex = Regex("/?(\\w+)\\.vdmsl")

    override val files: List<VdmFile> by lazy {
        val manifest = javaClass.getResource("/vdm/specification.mf").readText()
        manifest.lines().filter { it.isNotBlank() }.map {
            val name = vdmFileRegex.find(it)?.groups?.get(1)?.value ?: throw IllegalStateException("Unable to identify module name from specification manifest")
            VdmFile(name, javaClass.getResource("/$it").readText())
        }
    }

    val structure: SpecificationStructure by lazy {
        getStructure(this)
    }
}

class AnimationSpecification(val baseSpecification: VdmSpecification = BaseSpecification, vararg val additionalFiles: VdmFile) :
    VdmSpecification {
    override val files: List<VdmFile>
        get() = baseSpecification.files + additionalFiles
}
