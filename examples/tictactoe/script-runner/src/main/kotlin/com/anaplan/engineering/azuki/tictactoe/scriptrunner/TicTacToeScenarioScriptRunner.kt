package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.core.runner.oracle.MultiOracleScenarioRunner
import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.runner.ScenarioScriptRunner
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedName
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import org.apache.logging.log4j.core.config.Configurator
import java.io.File
import java.util.UUID
import kotlin.system.exitProcess

typealias OracleScenarioResult = MultiOracleScenarioRunner.Result<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>

typealias OracleScenarioOracleResult = MultiOracleScenarioRunner.OracleResult<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>

fun MultiOracleScenarioRunner.OracleResult<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>.findTask(
    taskType: TaskType
) = taskResults.find { it.taskType == taskType }

fun MultiOracleScenarioRunner.OracleResult<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>.hasTask(
    taskType: TaskType
) = taskResults.any { it.taskType == taskType }

object Command : CliktCommand(name = "scenario-runner") {

    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showRequiredTag = true, showDefaultValues = true) }
        }
    }

    override fun help(context: Context) = "Scenario script runner for guided scenario generation."

    val scriptFile by argument("script_file").file(mustExist = true,
        mustBeReadable = true,
        canBeFile = true,
        canBeDir = false)

    val script by lazy { scriptFile.readText() }
    val scenarioName by lazy { scriptFile.nameWithoutExtension }

    val oracleImplementationInstances by option("-o",
        "--oracleImplementationInstance",
        metavar = "impl",
        help = "Oracle implementation instance used to verify test system results").multiple()

    val testImplementationInstance by option("-t",
        "--testImplementationInstance",
        metavar = "impl",
        help = "Implementation instance under test").required()

    val importFile by option("-i",
        "--importFile",
        metavar = "file",
        help = "Imports to include in scriptified scenarios").file(mustExist = true,
        mustBeReadable = true,
        canBeFile = true,
        canBeDir = false)

    val imports by lazy { importFile?.readText() ?: "" }

    val testPackageName by option("-p",
        "--testPackageName",
        metavar = "name",
        help = "Package name to use for generated tests").default("com.anaplan.engineering.azuki.tictactoe.generated")

    val testClassName by option("-c", "--testClassName", help = "Class name to use for generated tests")

    val outputDir by option("-d", "--outputDir", metavar = "dir", help = "Directory in which to generate output").file(
        mustExist = false,
        canBeDir = true,
        canBeFile = false).defaultLazy("parent of <script_file>") { scriptFile.parentFile }

    private val verifiedTestDir by option("-v",
        "--verifiedTestDir",
        metavar = "dir",
        help = "Directory in which to store generated and verified JUnit tests").file(mustExist = false,
        canBeDir = true,
        canBeFile = false).defaultLazy("<outputDir>/verified") { File(outputDir, "verified") }

    private val unverifiedTestDir by option("-u",
        "--unverifiedTestDir",
        metavar = "dir",
        help = "Directory in which to store generated but unverified JUnit tests").file(mustExist = false,
        canBeDir = true,
        canBeFile = false).defaultLazy("<outputDir>/unverified") { File(outputDir, "unverified") }

    val resultSummaryFileName by option("-r",
        "--resultSummaryFileName",
        metavar = "filename",
        help = "File name to use when storing result summary").default("result.json")

    val queryResultsFileName by option("-q",
        "--queryResultsFileName",
        metavar = "filename",
        help = "File name to use when storing query results").default("queries.json")

    override fun run() {
        outputDir.mkdirs()
        verifiedTestDir.mkdirs()
        unverifiedTestDir.mkdirs()
        System.setProperty("logFileName", File(outputDir, "scenarioRun.log").absolutePath)
        Configurator.reconfigure()
        val runner = ScenarioScriptRunner(testImplementationInstance,
            oracleImplementationInstances,
            imports,
            TicTacToeResultsProcessor(
                scenarioName = scenarioName,
                generatedTestName = QualifiedName.create(testPackageName, testClassName ?: "Generated_${UUID.randomUUID()}"),
                outputDir = outputDir,
                resultSummaryFileName = resultSummaryFileName,
                queryResultsFileName = queryResultsFileName,
                verifiedTestsDir = verifiedTestDir,
                unverifiedTestsDir = unverifiedTestDir,
            ))
        try {
            runner.runScenario(script)
        } catch (e: Exception) {
            ScenarioScriptRunner.exit(e.message!!, ExitCode.UnknownError)
        }

        // something appears to be keeping this alive -- can't figure out
        // using exit as last resort for now!
        exitProcess(ExitCode.Ok.ordinal)
    }
}

fun main(args: Array<String>) = Command.main(args)
