package com.anaplan.engineering.azuki.core.system


import com.anaplan.engineering.azuki.core.JvmSystemProperties.eacMetadataDirPropertyName
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

typealias FunctionalElement = Int
typealias Behavior = Int

const val unsupportedBehavior: Behavior = -1

interface ReifiedBehavior  {
    val behavior: Behavior
}

data class EacMetadata(
    val functionalElement: FunctionalElement,
    val behavior: Behavior,
    val behaviorSummary: String,
    val methodName: String,
    val acceptanceCriteria: String,
    val implementation: String,
) {
    val scenarioName = "${functionalElement}-${behavior}-${methodName}"
}

internal val objectMapper = ObjectMapper().registerModule(KotlinModule())

object EacMetadataRecorder {

    private val metadataDir by lazy { File(java.lang.System.getProperty(eacMetadataDirPropertyName)) }

    val recording by lazy { java.lang.System.getProperties().containsKey(eacMetadataDirPropertyName) }

    fun record(metadata: EacMetadata) {
        val implDir = File(metadataDir, metadata.implementation)
        if (!implDir.exists()) {
            implDir.mkdirs()
        }
        val metadataFile = File(implDir, "${metadata.scenarioName}.json")
        objectMapper.writeValue(metadataFile, metadata)
    }
}

