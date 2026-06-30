package com.anaplan.engineering.azuki.rightofway.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.runner.ScenarioScriptRunner
import com.anaplan.engineering.azuki.rightofway.adapter.api.*
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.rightofway.adapter.scriptgen.RightOfWayScriptGeneration
import com.anaplan.engineering.azuki.verify.generation.JUnitTestCaseWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.File

class RightOfWayResultsProcessor(
    private val scenarioName: String,
    private val outputDir: File,
    private val resultSummaryFileName: String,
    verifiedTestsDir: File,
    unverifiedTestsDir: File,
    generatedTestPackage: String,
    generatedTestClass: String?,
    queryResultsFileName: String,
) : ScenarioScriptRunner.ResultProcessor<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory> {

    private val jUnitTestCaseWriter: JUnitTestCaseWriter<RightOfWayActionFactory, RightOfWayCheckFactory, RightOfWayQueryFactory, RightOfWayActionGeneratorFactory> =
        JUnitTestCaseWriter(
            RightOfWayScriptGeneration,
            RightOfWayRunnableScenarioClassGenerator,
            verifiedTestsDir, unverifiedTestsDir, generatedTestPackage, generatedTestClass,
        )
    private val queryResultsWriter = QueryResultWriter(outputDir, queryResultsFileName)
    private val generatedScenarioWriter = GeneratedScenarioWriter(scenarioName, outputDir)

    override fun processOracleScenario(result: OracleScenarioResult) {
        Log.debug("Processing oracle scenario results: result={}", result)
        generatedScenarioWriter.writeGeneratedScenario(result)
        val testFile = jUnitTestCaseWriter.writeTestCase(result)
        queryResultsWriter.writeQueryResults(scenarioName, result)
        val runResult = recordOracleScenarioResult(result, testFile)
        Log.info("Scenario completed result={}", runResult)
        when (runResult) {
            RightOfWayScenarioRun.Result.Incomplete -> ScenarioScriptRunner.exit("Invalid scenario", ExitCode.InvalidScenario)
            RightOfWayScenarioRun.Result.Errored -> ScenarioScriptRunner.exit("Unknown error", ExitCode.UnknownError)
            RightOfWayScenarioRun.Result.Unverified -> ScenarioScriptRunner.exit("Verification failed", ExitCode.VerificationFailed)
            else -> {} // do nothing
        }
    }

    private fun recordOracleScenarioResult(result: OracleScenarioResult, testFile: File?): RightOfWayScenarioRun.Result {
        val resultFile = File(outputDir, resultSummaryFileName)
        val verifyingOracleResults = result.oracleResults.lastOrNull()
        val verifyTask = verifyingOracleResults?.findTask(TaskType.Verify)
        val erroredTask = verifyingOracleResults?.taskResults?.find { it.exception != null }
        val runResult = if (erroredTask != null) {
            when (erroredTask.taskType) {
                TaskType.Query, TaskType.Verify -> RightOfWayScenarioRun.Result.Errored
                else -> RightOfWayScenarioRun.Result.Incomplete
            }
        } else {
            when (verifyTask?.result) {
                true -> RightOfWayScenarioRun.Result.Verified
                false -> RightOfWayScenarioRun.Result.Unverified
                else -> RightOfWayScenarioRun.Result.Incomplete
            }
        }
        val errorText = erroredTask?.exception?.summary()
        val totalDuration = result.oracleResults.sumOf { oracleResult ->
            oracleResult.taskResults.sumOf { it.duration ?: 0L }
        }
        resultFile.writeText(
            objectMapper.writeValueAsString(
                RightOfWayScenarioRun(
                    runResult,
                    totalDuration,
                    errorText,
                    testFile?.absolutePath
                )
            )
        )
        result.oracleResults.forEach { recordOutAndErr(it) }
        return runResult
    }

    private fun recordOutAndErr(oracleResult: OracleScenarioOracleResult) {
        val outFile = File(outputDir, "$scenarioName-${oracleResult.instance.instanceName}.out")
        val out = oracleResult.taskResults.mapNotNull { it.log.output }.joinToString("\n")
        outFile.writeText(out)
        val errFile = File(outputDir, "$scenarioName-${oracleResult.instance.instanceName}.err")
        val err = oracleResult.taskResults.mapNotNull { it.log.error }.joinToString("\n")
        errFile.writeText(err)
    }

    override fun handleError(error: Throwable) {
        Log.error("Error processing scenario", error)
        val resultFile = File(outputDir, resultSummaryFileName)
        resultFile.writeText(
            objectMapper.writeValueAsString(
                RightOfWayScenarioRun(
                    RightOfWayScenarioRun.Result.Errored,
                    0,
                    error.summary(),
                    null,
                )
            )
        )
    }

    private fun Throwable.summary() = "${message}\n${stackTrace.joinToString(separator = "\n", limit = 10)}"

    companion object {

        private val Log = LoggerFactory.getLogger(RightOfWayResultsProcessor::class.java)
        private val objectMapper = jacksonObjectMapper()
    }
}
