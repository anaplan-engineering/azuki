package com.anaplan.engineering.azuki.core.system


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

internal const val eacMetadataDirProperty = "eac.metadata.dir"
internal const val eacReportDirProperty = "eac.report.dir"

object EacMetadataRecorder {

    private val metadataDir by lazy { File(java.lang.System.getProperty(eacMetadataDirProperty)) }

    val recording by lazy { java.lang.System.getProperties().containsKey(eacMetadataDirProperty) }

    fun record(metadata: EacMetadata) {
        val implDir = File(metadataDir, metadata.implementation)
        if (!implDir.exists()) {
            implDir.mkdirs()
        }
        val metadataFile = File(implDir, "${metadata.scenarioName}.json")
        objectMapper.writeValue(metadataFile, metadata)
    }
}

