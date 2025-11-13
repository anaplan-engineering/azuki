package com.anaplan.engineering.azuki.listeners

import com.anaplan.engineering.azuki.core.runner.AzukiRunListener
import com.anaplan.engineering.azuki.core.runner.ScenarioResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.util.concurrent.LinkedBlockingQueue

class JsonResultRecorder : AzukiRunListener {

    object SystemProperties {
        /**
         * If present Azuki will write EAC metadata to the specified directory
         */
        const val resultDirPropertyName = "com.anaplan.engineering.azuki.listeners.json.resultdir"
    }

    private val resultDir by lazy {
        val resultDirPath = System.getProperty(SystemProperties.resultDirPropertyName) ?: "."
        File(resultDirPath)
    }

    private val results = LinkedBlockingQueue<ScenarioResult>()

    override fun scenarioComplete(scenarioResult: ScenarioResult) {
        results.add(scenarioResult)
    }

    private val objectMapper by lazy { jacksonObjectMapper() }

    override fun suiteComplete(suiteName: String) {
        val suiteResults = mutableListOf<ScenarioResult>().apply {
            results.drainTo(this)
        }
        if (suiteResults.isNotEmpty()) {
            resultDir.mkdirs()
            suiteResults.groupBy {
                StringBuilder().apply {
                    append(it.implementationName)
                    if (it.implementationVersion != null) {
                        append("-${it.implementationVersion}")
                    }
                    if (it.persistenceImplementationName != null) {
                        append("-${it.persistenceImplementationName}")
                    }
                    if (it.persistenceImplementationVersion != null) {
                        append("-${it.persistenceImplementationVersion}")
                    }
                }.toString()
            }.forEach { (name, results) ->
                val file = File(resultDir, "$name-$suiteName.json")
                objectMapper.writeValue(file, results.toTypedArray())
            }
        }
    }

    override fun runComplete() {
        suiteComplete("run")
    }
}
