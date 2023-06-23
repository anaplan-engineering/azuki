package com.anaplan.engineering.azuki.verify.orchestrator.configuration

import com.google.common.io.Files
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class RunConfiguration(
    val batchName: String = "batch" + DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(LocalDateTime.now()),
    val scenarios: ScenarioConfiguration = ScenarioConfiguration(),
    val admin: AdminConfiguration = AdminConfiguration(),
) {

    val runDir by lazy {
        val resultDirPath = admin.resultDir
        if (resultDirPath == null) Files.createTempDir() else File(resultDirPath).resolve(batchName)
    }

    val testPackage by lazy { "${scenarios.testPackageRoot}.$batchName" }

    val verifiedTestsDir by lazy {
        File(runDir, "tests/verified/$batchName")
    }

    val unverifiedTestsDir by lazy {
        File(runDir, "tests/unverified/$batchName")
    }
}

data class ScenarioConfiguration(
    val generateTests: Boolean = false,
    val testPackageRoot: String = "generated",
    val importFile: String = "imports",
)

data class AdminConfiguration(
    val maxParallelRuns: Int = 4,
    val resultDir: String? = null,
    val fork: ForkConfiguration = ForkConfiguration(),
    val killAgentPath: String? = null,
    val report: ReportConfiguration = ReportConfiguration(),
)

data class ForkConfiguration(
    val runnerMainClass: String = "com.anaplan.engineering.azuki.runner.ScenarioScriptRunnerKt",
    val jvmArgs: List<String> = emptyList(),
    val environment: Map<String, String> = emptyMap(),
    val systemProperties: Map<String, String> = emptyMap(),
    val timeout: Long? = null,
    /**
     * If true, will use custom tmp directory and delete when fork completes. This prevents issues with
     * Files.deleteOnExit() which will not delete files if the fork times out.
     */
    val encapsulateTmp: Boolean = false,
)

data class ReportConfiguration(
    val prefix: String = "report",
    val generateEvery: Long = 0L
)



