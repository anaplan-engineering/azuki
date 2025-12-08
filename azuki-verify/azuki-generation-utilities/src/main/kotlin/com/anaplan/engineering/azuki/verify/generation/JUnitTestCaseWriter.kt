package com.anaplan.engineering.azuki.verify.generation

import com.anaplan.engineering.azuki.core.runner.ImplementationInstance
import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.core.runner.oracle.MultiOracleScenarioRunner
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import com.anaplan.engineering.azuki.script.generation.runnable.KotlinName
import com.anaplan.engineering.azuki.script.generation.runnable.RunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import org.slf4j.LoggerFactory
import java.io.File
import java.util.UUID

/**
 * Helper class for writing JUnit test cases produced by a scenario runner.
 */
class JUnitTestCaseWriter<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory>(
    private val generateScript: ScriptGenerationService<AF, CF, QF, QF, AGF, *, *>,
    private val generateRunnable: RunnableScenarioClassGenerator,
    private val verifiedTestsDir: File,
    private val unverifiedTestsDir: File,
    private val generatedTestClass: KotlinName,
) {

    fun writeTestCase(result: MultiOracleScenarioRunner.Result<AF, CF, QF, AGF>): File? {
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
        @Suppress("UNCHECKED_CAST") val answers = queryTaskResult?.result as? List<Answer<*, CF>>
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
        baseScenario: BuildableScenario<AF>,
        answers: List<Answer<*, CF>>,
        testImplementation: ImplementationInstance<AF, CF, QF, AGF>,
        verifyingImplementation: ImplementationInstance<AF, CF, QF, AGF>,
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
        baseScenario: BuildableScenario<AF>,
        answers: List<Answer<*, CF>>,
        testImplementation: ImplementationInstance<AF, CF, QF, AGF>,
        verifyingImplementation: ImplementationInstance<AF, CF, QF, AGF>,
    ) = generateRunnable.generate(
        className = generatedTestClass,
        scenarioScript = generateScript.generateVerifiableScenario(baseScenario, answers),
        implementationVersions = mapOf(
            testImplementation.implementationName to (testImplementation.version ?: "0.0.0"),
            verifyingImplementation.implementationName to (verifyingImplementation.version ?: "0.0.0"),
        ))

    companion object {

        private val Log = LoggerFactory.getLogger(JUnitTestCaseWriter::class.java)
    }

    fun MultiOracleScenarioRunner.OracleResult<AF, CF, QF, AGF>.findTask(
        taskType: TaskType
    ) = taskResults.find { it.taskType == taskType }

    fun MultiOracleScenarioRunner.OracleResult<AF, CF, QF, AGF>.hasTask(
        taskType: TaskType
    ) = taskResults.any { it.taskType == taskType }
}
