package com.anaplan.engineering.azuki.verify.batch.api

import java.io.File

interface OrchestratableScenario {
    val name: String
    val script: String
    fun getResultDirectory(baseDir: File): File
}


