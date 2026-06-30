package com.anaplan.engineering.vdmanimation.vdmj

import com.anaplan.engineering.vdmanimation.api.*
import com.fujitsu.vdmj.ExitStatus
import com.fujitsu.vdmj.`in`.definitions.INExplicitFunctionDefinition
import com.fujitsu.vdmj.`in`.definitions.INLocalDefinition
import com.fujitsu.vdmj.`in`.definitions.INTypeDefinition
import com.fujitsu.vdmj.`in`.definitions.INValueDefinition
import com.fujitsu.vdmj.`in`.patterns.INIdentifierPattern
import com.fujitsu.vdmj.lex.Dialect
import com.fujitsu.vdmj.messages.ConsolePrintWriter
import com.fujitsu.vdmj.runtime.ContextException
import com.fujitsu.vdmj.runtime.ModuleInterpreter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import org.slf4j.LoggerFactory
import java.io.PrintWriter
import java.io.StringWriter

class VDMJAnimator : VdmAnimator {

    override val engine = VdmEngine.VDMJ

    private val coverageGenerator = CoverageGenerator()

    override fun animate(animationTarget: AnimationTarget, animationContext: AnimationContext): AnimationResult {
        val tmpDir = Files.createTempDirectory("vdmj-animator-")
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
        val controller = parseAndTypeCheck(Dialect.VDM_SL, specificationFiles, context)
        return controller.toolLifeCycle.interpreter as ModuleInterpreter
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
            // TODO LF: remove as already called upon lazy initialisation already?
            interpreter.init()
            interpreter.defaultName = module
            // VDMJ needs at least a global environment, which is created for just a string execute
            val value = interpreter.execute("$operation()")
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
            val printWriter = ConsolePrintWriter(PrintWriter(stringWriter))
            printWriter.print("\n${e.message}\n")
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
        val tmpDir = Files.createTempDirectory("vdmj-animator-")
        try {
            val interpreter = interpretSpecification(specification, AnimationContext(), tmpDir)
            return SpecificationStructure(interpreter.modules.map { m ->
                val n = m
                val types = m.defs.filterIsInstance<INTypeDefinition>().map {
                    it.name.name
                }
                // when exported explicitly values are locals, otherwise they are values
                val locals = m.defs.filterIsInstance<INLocalDefinition>().map { it.valueDefinition }
                val values = (locals + m.defs.filterIsInstance<INValueDefinition>()).map {
                    if (it.name == null) {
                        val pattern = it.pattern
                        if (pattern is INIdentifierPattern) {
                            pattern.name.name
                        } else {
                            throw VdmSpecificationException("Cannot create VDM spec structure")
                        }
                    } else {
                        it.name.name
                    }
                }
                val functions = m.defs.filterIsInstance<INExplicitFunctionDefinition>().map { it.name.name }
                m.name.name to Module(m.name.name, types, values, functions)
            }.toMap())
        } finally {
            if (System.getProperty("retainOvertureTempFiles")?.toBoolean() != true) {
                tmpDir.toFile().deleteRecursively()
            }
        }

    }

    private fun parseAndTypeCheck(dialect: Dialect, specification: List<File>, animationContext: AnimationContext) : VDMJController {
        val controller = VDMJController(dialect = dialect, showW = !animationContext.quiet, files = specification)
        val parseStatus = controller.toolLifeCycle.parse()
        if (parseStatus != ExitStatus.EXIT_OK) {
            throw VdmSpecificationException("VDM parse failed")
        }
        val typeCheckStatus = controller.toolLifeCycle.typeCheck()
        if (typeCheckStatus != ExitStatus.EXIT_OK) {
            throw VdmSpecificationException("VDM type check failed")
        }
        return controller
    }

    companion object {
        val Log = LoggerFactory.getLogger(VDMJAnimator::class.java)
    }
}

