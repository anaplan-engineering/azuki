package com.anaplan.engineering.azuki.resultreporter

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.jvm.java

/**
 * Intended for use with com.anaplan.engineering.azuki.listeners.JsonResultRecorder
 */
class JsonResultReporterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("azukiResultReport", JsonResultReporterPluginExtension::class.java, project.objects)
        project.tasks.create(
            mapOf<String, Any>(
                "name" to "createScenarioReport",
                "type" to ScenarioReportGeneratorTask::class.java,
                "group" to "azuki"
            )
        )
    }
}

@CacheableTask
abstract class ScenarioReportGeneratorTask : DefaultTask() {

    @TaskAction
    fun apply() {
        val config = project.extensions.findByType(JsonResultReporterPluginExtension::class.java) ?: Config.Default
        val sourceFiles = config.sourceDirs.map { File(it) }.flatMap { project.fileTree(it).filter { f ->
            f.name.startsWith("scenario-results") && f.name.endsWith("json")
        } }
        val reportDir = File(config.reportDir).apply { mkdirs() }
        ScenarioReportGenerator(sourceFiles, reportDir).generate()
    }
}

interface Config {
    var sourceDirs: List<String>
    var reportDir: String

    object Default : Config {
        override var sourceDirs = emptyList<String>()
        override var reportDir = "."
    }
}

open class JsonResultReporterPluginExtension @javax.inject.Inject constructor(objectFactory: ObjectFactory) : Config {
    override var sourceDirs: List<String> = emptyList()
    override var reportDir: String = "."
}
