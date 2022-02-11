package com.anaplan.engineering.vdmstubgenerator

import com.anaplan.engineering.vdmanimation.api.VdmAnimatorKt
import com.anaplan.engineering.vdmanimation.api.VdmFile
import com.anaplan.engineering.vdmanimation.api.VdmSpecification
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction

// TODO -- remove horrible hack to handle duplicate undef!
abstract class VdmStubGenerationTask extends DefaultTask {

    abstract Property<String> getTargetPackage()
    abstract Property<File> getSpecificationManifest()
    abstract Property<File> getSpecificationDir()

    @TaskAction
    def generateDefinitions() {
        def targetPackage = getTargetPackage().getOrElse("com.anaplan.engineering.vdmstubgenerator.generated")
        def manifestFile = getSpecificationManifest().getOrElse(new File(project.projectDir,"build/generated-resources/vdm/specification.mf"))
        def specDir = getSpecificationDir().getOrElse(new File(project.projectDir,"build/generated-resources/"))

        int i = 0;
        def specification = new Specification(files: manifestFile.readLines().collect { file ->
            new VdmFile("${i++}.vdmsl", new File(specDir,"$file").text)
        })
        def structure = VdmAnimatorKt.getStructure(specification)


        def defsFile = new File(project.projectDir,"build/generated-sources/SpecificationDefinitions.kt")
        defsFile.parentFile.mkdirs()
        defsFile.text = """
package $targetPackage

import com.anaplan.engineering.azuki.vdm.VdmDeclarationType
import com.anaplan.engineering.vdmanimation.api.Definition
import com.anaplan.engineering.vdmanimation.api.DefinitionImport

sealed class SpecificationModule(val moduleName: String) {
    fun type(name: String) = SpecificationType(moduleName, name)
    fun function(name: String) = SpecificationFunction(moduleName, name)
    fun value(name: String) = SpecificationValue(moduleName, name)
    override fun toString() = moduleName
}

class SpecificationValue(module: String, definition: String) : SpecificationDefinition(module, Definition.Type.value, definition)
class SpecificationType(module: String, definition: String) : SpecificationDefinition(module, Definition.Type.type, definition), VdmDeclarationType
class SpecificationPrecondition(module: String, function: String) : SpecificationDefinition(module,Definition.Type.function, "pre_\$function")
class SpecificationPostcondition(module: String, function: String) : SpecificationDefinition(module,Definition.Type.function, "post_\$function")
class SpecificationFunction(module: String, definition: String) : SpecificationDefinition(module, Definition.Type.function, definition) {

    val pre by lazy {
        SpecificationPrecondition(module, definition)
    }

    val post by lazy {
        SpecificationPostcondition(module, definition)
    }

}

abstract class SpecificationDefinition(
        val module: String,
        val type: Definition.Type,
        val definition: String
) {
    override fun toString() = "\$module`\$definition"

    val import by lazy {
        DefinitionImport(module, type, definition)
    }
}


""" + structure.modules.values().collect { module ->
            """
object ${module.name}Module: SpecificationModule(\"${module.name}\") {
${
                module.definitions.values().collect { dfn ->
                    "    val ${dfn.name.replace("\$", "_")} = ${dfn.type.name()}(\"${dfn.name.replace("\$", "\\\$")}\")"
                }.join("\n")
            }
}
"""
        }.join("\n")

    }

}

class Specification implements VdmSpecification {
    List<VdmFile> files
}
