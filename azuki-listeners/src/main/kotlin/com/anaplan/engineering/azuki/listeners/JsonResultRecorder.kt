package com.anaplan.engineering.azuki.listeners

import com.anaplan.engineering.azuki.core.runner.AzukiRunListener
import com.anaplan.engineering.azuki.core.runner.ScenarioResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
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

    private val objectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    override fun suiteComplete(suiteName: String) {
        val suiteResults = mutableListOf<ScenarioResult>().apply {
            results.drainTo(this)
        }
        if (suiteResults.isNotEmpty()) {
            resultDir.mkdirs()
            val file = File(resultDir, "scenario-results-$suiteName.json")
            objectMapper.writeValue(file, suiteResults.toTypedArray())
        }
    }

    override fun runComplete() {
        val runResults = mutableListOf<ScenarioResult>().apply {
            results.drainTo(this)
        }
        if (runResults.isNotEmpty()) {
            resultDir.mkdirs()
            val file = File(resultDir, "scenario-results.json")
            objectMapper.writeValue(file, runResults.toTypedArray())
        }
    }
}
