package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGeneration
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class JUnitTestCaseWriter(
    private val verifiedTestsDir: File,
    private val unverifiedTestsDir: File,
    private val generatedTestPackage: String,
    private val generatedTestClass: String?,
) {

    fun writeTestCase(result: OracleScenarioResult): File? {
        val verifyingOracle = result.oracleResults.lastOrNull()
        if (verifyingOracle == null) {
            Log.debug("Skipping generation of test case as have no oracle results")
            return null
        }
        if (!verifyingOracle.hasTask(TaskType.Verify)) {
            Log.debug("Skipping generation of test case as verification task did not run")
            return null
        }
        val queryTaskResult = result.oracleResults.mapNotNull { it.findTask(TaskType.Query) }.lastOrNull()
        @Suppress("UNCHECKED_CAST") val answers = queryTaskResult?.result as? List<Answer<*, TicTacToeCheckFactory>>
        if (answers == null) {
            Log.error("Skipping generation of test case as query result is unexpectedly missing answers")
            return null
        }
        val testCaseDir = if (result.verified) verifiedTestsDir else unverifiedTestsDir
        return writeTestCase(queryTaskResult.scenario,
            answers,
            result.testInstance,
            verifyingOracle.instance,
            testCaseDir)
    }

    private fun writeTestCase(
        baseScenario: BuildableScenario<TicTacToeActionFactory>,
        answers: List<Answer<*, TicTacToeCheckFactory>>,
        testImplementation: TicTacToeImplementationInstance,
        verifyingImplementation: TicTacToeImplementationInstance,
        targetDir: File
    ): File {
        val runnableScenarioClass =
            createRunnableScenario(baseScenario, answers, testImplementation, verifyingImplementation)
        targetDir.mkdirs()
        Log.debug("Writing test class={} targetDir={}", runnableScenarioClass.className, targetDir)
        val testFile = File(targetDir, "${runnableScenarioClass.className}.kt")
        val definition = ScenarioFormatter.formatScenario(runnableScenarioClass.definition)
        testFile.writeText(definition)
        return testFile
    }

    private fun createRunnableScenario(
        baseScenario: BuildableScenario<TicTacToeActionFactory>,
        answers: List<Answer<*, TicTacToeCheckFactory>>,
        testImplementation: TicTacToeImplementationInstance,
        verifyingImplementation: TicTacToeImplementationInstance,
    ) = TicTacToeRunnableScenarioClassGenerator.generate(
        // TODO - add utility function for arbitrary name
        className = generatedTestClass ?: ("Generated_" + UUID.randomUUID().toString().replace("-", "_")),
        packageName = generatedTestPackage,
        scenarioScript = TicTacToeScriptGeneration.patterns.verifiableScenarioFromOracle(baseScenario, answers).render {
            indent = 2
            format = false
        },
        implementationVersions = mapOf(
            testImplementation.implementationName to (testImplementation.version ?: "0.0.0"),
            verifyingImplementation.implementationName to (verifyingImplementation.version ?: "0.0.0"),
        ))

    companion object {

        private val Log = LoggerFactory.getLogger(JUnitTestCaseWriter::class.java)
    }
}
