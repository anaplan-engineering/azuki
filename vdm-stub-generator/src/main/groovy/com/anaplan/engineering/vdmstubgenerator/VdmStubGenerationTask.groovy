package com.anaplan.engineering.vdmstubgenerator

import com.anaplan.engineering.vdmanimation.api.VdmAnimatorKt
import com.anaplan.engineering.vdmanimation.api.VdmFile
import com.anaplan.engineering.vdmanimation.api.VdmSpecification
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

// TODO -- remove horrible hack to handle duplicate undef!
abstract class VdmStubGenerationTask extends DefaultTask {

    @Input
    abstract Property<String> getTargetPackage()

    // Typically used for specification files located in a jar
    @Optional
    @InputDirectory
    abstract Property<File> getSpecificationDirectory()

    // Typically used for local specification files
    @Optional
    @Input
    abstract Property<String> getSpecificationManifest()

    @OutputDirectory
    abstract Property<File> getStubDirectory()

    @TaskAction
    def generateDefinitions() {
        def targetPackage = getTargetPackage().getOrElse("com.anaplan.engineering.vdmstubgenerator.generated")
        def vdmFiles = []
        def manifestUrl = getClass().getResource(getSpecificationManifest().getOrElse("/vdm/specification.mf"))
        if (manifestUrl != null) {
            vdmFiles.addAll(manifestUrl.readLines().indexed().collect { i, file ->
                    new VdmFile("$i.vdmsl", getClass().getResource("/$file").text)
            })
        }
        def specificationDirectory = getSpecificationDirectory().getOrNull()
        if (specificationDirectory != null) {
            vdmFiles.addAll(project.fileTree(specificationDirectory).findAll { f ->
                f.name.endsWith("vdmsl")
            }.collect { file ->
                new VdmFile(file.name, file.text)
            })
        }
        if (vdmFiles.isEmpty()) {
            logger.warn("No specification files found, manifestUrl=$manifestUrl specificationDirectory=$specificationDirectory")
        }

        def specification = new Specification(files: vdmFiles)
        def structure = VdmAnimatorKt.getStructure(specification)

        def stubDirectory = getStubDirectory().getOrElse(new File(project.projectDir, "build/generated-sources"))
        stubDirectory.mkdirs()
        def defsFile = new File(stubDirectory, "SpecificationDefinitions.kt")
        defsFile.text = """
package  $targetPackage

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


 """ + structure.modules.values().collect {
            module ->
                """
object ${module.name}Module: SpecificationModule(\"${module.name}\") {
${
                    module.definitions.values().collect { dfn ->
                        "    val ${dfn.name.replace("\$", "_")} = ${dfn.type.name()}(\"${dfn.name.replace("\$", "\\\$")}\")"
                    }.join("\n")
                }
}
"""
        }.

            join("\n")

    }

}

class Specification implements VdmSpecification {
    List<VdmFile> files
}
