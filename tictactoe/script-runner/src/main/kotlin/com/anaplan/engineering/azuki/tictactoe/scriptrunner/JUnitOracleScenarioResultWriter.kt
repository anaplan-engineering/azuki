package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class JUnitOracleScenarioResultWriter(
    private val junitReportDir: File
) {

    private enum class TestState {
        Skipped,
        Failed,
        Errored,
        Passed
    }

    private data class TestResult(
        val name: String,
        val state: TestState,
        val durationMs: Long,
        val message: String?
    )


    fun writeTestResults(scenarioName: String, result: OracleScenarioResult) =
        result.oracleResults.forEach {
            writeTestResults(scenarioName, it)
        }

    private fun writeTestResults(scenarioName: String, oracleResult: OracleScenarioOracleResult) {
        val testResults = generateTestResults(oracleResult)
        val suiteName = "$scenarioName-${oracleResult.instance.instanceName}"
        val out = oracleResult.taskResults.mapNotNull { it.log.output }.joinToString("\n")
        val err = oracleResult.taskResults.mapNotNull { it.log.error }.joinToString("\n")
        writeTestResults(suiteName, oracleResult.start, out, err, testResults)
    }

    private fun generateTestResults(oracleResult: OracleScenarioOracleResult): List<TestResult> {
        val results = mutableListOf<TestResult>()

        val declarationCheck = oracleResult.findTask(TaskType.CheckDeclarations)
        if (declarationCheck != null) {
            results.add(
                TestResult(
                    name = TaskType.CheckDeclarations.name,
                    state = when {
                        declarationCheck.exception != null -> TestState.Errored
                        declarationCheck.result == true -> TestState.Passed
                        else -> TestState.Skipped
                    },
                    durationMs = declarationCheck.duration.toMs(),
                    message = when {
                        declarationCheck.exception != null -> declarationCheck.exception!!.message
                        declarationCheck.result == true -> null
                        else -> "Declaration is invalid - skipping scenario"
                    }
                )
            )
        }

        val actionsCheck = oracleResult.findTask(TaskType.CheckActions)
        if (actionsCheck != null) {
            results.add(
                TestResult(
                    name = TaskType.CheckActions.name,
                    state = when {
                        actionsCheck.exception != null -> TestState.Errored
                        actionsCheck.result == true -> TestState.Passed
                        else -> TestState.Skipped
                    },
                    durationMs = actionsCheck.duration.toMs(),
                    message = when {
                        actionsCheck.exception != null -> actionsCheck.exception!!.message
                        actionsCheck.result == true -> null
                        else -> "Test actions are invalid - skipping scenario"
                    }
                )
            )
        }

        val query = oracleResult.findTask(TaskType.Query)
        if (query != null) {
            results.add(
                TestResult(
                    name = TaskType.Query.name,
                    state = when {
                        query.exception != null -> TestState.Errored
                        (query.result as List<*>?).isNullOrEmpty() -> TestState.Skipped
                        else -> TestState.Passed
                    },
                    durationMs = query.duration.toMs(),
                    message = when {
                        query.exception != null -> query.exception!!.message
                        else -> "Queries completed"
                    }
                )
            )
        }

        val verify = oracleResult.findTask(TaskType.Verify)
        if (verify != null) {
            results.add(
                TestResult(
                    name = TaskType.Verify.name,
                    state = when {
                        verify.exception != null -> TestState.Errored
                        verify.result == true -> TestState.Passed
                        else -> TestState.Failed
                    },
                    durationMs = verify.duration.toMs(),
                    message = when {
                        verify.exception != null -> verify.exception!!.message
                        else -> verify.result.toString()
                    }
                )
            )
        }

        return results
    }

    private fun writeTestResults(
        suiteName: String,
        start: LocalDateTime,
        out: String,
        err: String,
        testResults: List<TestResult>
    ) {
        junitReportDir.mkdirs()
        val reportFile = File(junitReportDir, "TEST-${suiteName}.xml")
        Log.debug("Writing test report suite={} reportFile={}", suiteName, reportFile)
        val xml = testSuite {
            setTime(testResults.sumOf { it.durationMs })
            name = suiteName
            tests = testResults.size
            failures = testResults.count { it.state == TestState.Failed }
            errors = testResults.count { it.state == TestState.Errored }
            skipped = testResults.count { it.state == TestState.Skipped }
            timestamp = timestampFormatter.format(start)
            hostname = InetAddress.getLocalHost().hostName
            testResults.forEach { testResult ->
                testCase {
                    setTime(testResult.durationMs)
                    name = testResult.name
                    classname = suiteName
                    when (testResult.state) {
                        TestState.Errored -> error {
                            if (testResult.message != null) {
                                message = testResult.message
                            }
                        }
                        TestState.Failed -> failure {
                            if (testResult.message != null) {
                                message = testResult.message
                            }
                        }
                        TestState.Skipped -> skipped {}
                        else -> {
                        }
                    }
                }
            }
            systemOut {
                text = out
            }
            systemErr {
                text = err
            }
        }
        Files.write(reportFile.toPath(), xml.toByteArray())
    }

    companion object {
        private val Log = LoggerFactory.getLogger(JUnitOracleScenarioResultWriter::class.java)

        private val timestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}

