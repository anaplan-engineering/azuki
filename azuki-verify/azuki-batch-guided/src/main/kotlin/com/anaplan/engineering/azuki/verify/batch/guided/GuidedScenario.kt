package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.verify.batch.api.OrchestratableScenario
import java.io.File

data class GuidedScenario(
    override val name: String,
    override val script: String,
    val baseFile: File,
) : OrchestratableScenario {

    override fun getResultDirectory(baseDir: File) =
        baseDir.resolve("${baseFile.nameWithoutExtension}/$name")
}
