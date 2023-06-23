package com.anaplan.engineering.azuki.verify.orchestrator

import com.anaplan.engineering.azuki.verify.orchestrator.configuration.ForkConfiguration
import org.slf4j.LoggerFactory
import java.io.File

data class VerificationEnvironment(
    val testImplementationInstance: String,
    val oracleImplementationInstances: List<String>,
    val testPackage: String,
    val junitResultsDir: File,
    val verifiedTestsDir: File,
    val unverifiedTestsDir: File,
    val importFile: File,
    val killAgentBinary: File? = null,
    val javaHome: File = File(System.getProperty("java.home")),
    val forkConfiguration: ForkConfiguration,
) {
    init {
        if (!importFile.exists() || !importFile.isFile) {
            Log.warn("Import file does not exist or is not file: importFile=${importFile.absolutePath}")
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(VerificationEnvironment::class.java)
    }
}
