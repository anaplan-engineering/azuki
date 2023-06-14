package com.anaplan.engineering.vdmanimation.overture

import com.anaplan.engineering.vdmanimation.api.*
import org.overture.ast.definitions.ALocalDefinition
import org.overture.ast.definitions.ATypeDefinition
import org.overture.ast.definitions.AValueDefinition
import org.overture.ast.definitions.SFunctionDefinition
import org.overture.ast.patterns.AIdentifierPattern
import org.overture.interpreter.VDMSL
import org.overture.interpreter.runtime.ContextException
import org.overture.interpreter.runtime.ModuleInterpreter
import org.overture.interpreter.util.ExitStatus
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.slf4j.LoggerFactory
import java.io.PrintWriter
import java.io.StringWriter

class OvertureAnimator : VdmAnimator {

    private val coverageGenerator = CoverageGenerator()

    override fun animate(animationTarget: AnimationTarget, animationContext: AnimationContext): AnimationResult {
        val tmpDir = Files.createTempDirectory("overture-animator-")
        try {
            val interpreter = interpretSpecification(animationTarget.specification, animationContext, tmpDir)
            val checkResult = animate(interpreter, animationTarget.module, animationTarget.operation, animationContext)
            return AnimationResult(checkResult, coverageGenerator.generate(interpreter, animationTarget.module))
        } finally {
            if (System.getProperty("retainOvertureTempFiles")?.toBoolean() != true) {
                tmpDir.toFile().deleteRecursively()
            }
        }
    }

    private fun interpretSpecification(
        specification: VdmSpecification,
        context: AnimationContext,
        tmpDir: Path
    ): ModuleInterpreter {
        val specificationFiles = generateSpecificationFiles(specification, tmpDir)
        val controller = parseAndTypeCheck(specificationFiles, context)
        return controller.interpreter as ModuleInterpreter
    }

    private fun generateSpecificationFiles(specification: VdmSpecification, tmpDir: Path): List<File> {
        Log.info("Animating in $tmpDir")
        return specification.files.map { module ->
            val moduleFile = tmpDir.resolve("${module.name}.vdmsl").toFile()
            moduleFile.writeText(module.text)
            moduleFile
        }
    }

    private fun animate(
        interpreter: ModuleInterpreter,
        module: String,
        operation: String,
        animationContext: AnimationContext
    ): Boolean {
        try {
            interpreter.init(null)
            interpreter.defaultName = module
            val value = interpreter.execute("$operation()", null)
            return if (animationContext.expectFailure) {
                false
            } else {
                value.boolValue(null)
            }
        } catch (e: ContextException) {
            if (animationContext.expectFailure) {
                return true
            }

            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            printWriter.write("\n${e.message}\n")
            e.ctxt.printStackTrace(printWriter, true)
            Log.error(stringWriter.toString())

            when (e.number) {
                4072 -> throw VdmPostconditionFailure(e)
                4055 -> throw VdmPreconditionFailure(e)
                4060 -> throw VdmInvariantFailure(e)
                4087 -> throw VdmDeclarationFailure(e)
                else -> throw e
            }
        }
    }

    override fun getStructure(specification: VdmSpecification): SpecificationStructure {
        val tmpDir = Files.createTempDirectory("overture-animator-")
        try {
            val interpreter = interpretSpecification(specification, AnimationContext(), tmpDir)
            return SpecificationStructure(interpreter.modules.map { m ->
                val types = m.exportdefs.filterIsInstance<ATypeDefinition>().map {
                    it.name.name
                }
                // when exported explicitly values are locals, otherwise they are values
                val locals = m.exportdefs.filterIsInstance<ALocalDefinition>().map { it.valueDefinition }
                val values = (locals + m.exportdefs.filterIsInstance<AValueDefinition>()).map {
                    if (it.name == null) {
                        val pattern = it.pattern
                        if (pattern is AIdentifierPattern) {
                            pattern.name.name
                        } else {
                            throw VdmSpecificationException("Cannot create VDM spec structure")
                        }
                    } else {
                        it.name.name
                    }
                }
                val functions = m.exportdefs.filterIsInstance<SFunctionDefinition>().map { it.name.name }
                m.name.name to Module(m.name.name, types, values, functions)
            }.toMap())
        } finally {
            if (System.getProperty("retainOvertureTempFiles")?.toBoolean() != true) {
                tmpDir.toFile().deleteRecursively()
            }
        }

    }


    private fun parseAndTypeCheck(specification: List<File>, animationContext: AnimationContext): VDMSL {
        val controller = VDMSL()
        controller.setQuiet(animationContext.quiet)
        controller.setWarnings(!animationContext.quiet)
        val parseStatus = controller.parse(specification)
        if (parseStatus != ExitStatus.EXIT_OK) {
            throw VdmSpecificationException("VDM parse failed")
        }
        val typeCheckStatus = controller.typeCheck()
        if (typeCheckStatus != ExitStatus.EXIT_OK) {
            throw VdmSpecificationException("VDM type check failed")
        }
        return controller
    }

    companion object {
        val Log = LoggerFactory.getLogger(OvertureAnimator::class.java)
    }
}

