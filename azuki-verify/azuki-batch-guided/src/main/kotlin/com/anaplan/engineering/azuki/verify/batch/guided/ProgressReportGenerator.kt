package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.verify.batch.api.CompletedScenario
import com.anaplan.engineering.azuki.verify.batch.api.ScenarioResultContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URI

internal class ProgressReportGenerator<RC : ScenarioResultContext>(
    private val batchState: GuidedBatchState<RC>,
) {

    fun generate(runDir: File, fileName: String): File {
        val completedScenarios = batchState.completed
        Log.info("Generating summary report for ${completedScenarios.size} scenarios")
        val scripts = findFilesAndMapToUrl(runDir, "-gen-vfy.scn")
        val reports = findFilesAndMapToUrl(runDir, "scenario.log")
        val model = buildModel(completedScenarios, scripts, reports)
        val report = createHtmlReport(model)
        val reportFile = runDir.resolve(fileName)
        reportFile.parentFile.mkdirs()
        reportFile.writeText(report)
        copyResource("/vdm.css", runDir)
        Log.info("Summary report written")
        return reportFile
    }

    private fun copyResource(resource: String, dir: File) {
        val resourceText = javaClass.getResource(resource).readText()
        File(dir, resource).writeText(resourceText)
    }

    private fun findFilesAndMapToUrl(dir: File, suffix: String) =
        dir.walk().filter { it.name.endsWith(suffix) }
            .map { it.parentFile.name to dir.toURI().relativize(it.toURI()) }
            .toMap()

    private fun buildModel(
        completedScenarios: List<CompletedScenario<GuidedScenario, RC>>,
        scripts: Map<String, URI>,
        reports: Map<String, URI>
    ): ReportModel<RC> {
        val additionalFields = completedScenarios.flatMap { it.context?.reportFields?.keys ?: emptySet() }.distinct()
        val completedScenariosWithMetadata = completedScenarios.map { cs ->
            val scenarioName = cs.orchestratableScenario.name
            CompletedScenarioWithMetadata(
                id = scenarioName,
                completedScenario = cs,
                script = scripts[scenarioName],
                report = reports[scenarioName],
                fields = cs.context?.reportFields ?: emptyMap()
            )
        }
        return ReportModel(
            additionalFields = additionalFields,
            incomplete = completedScenariosWithMetadata.filter {
                it.completedScenario.exitCode.category == ExitCode.Category.Incomplete
            }.groupBy { it.completedScenario.orchestratableScenario.baseFile },
            unverified = completedScenariosWithMetadata.filter {
                it.completedScenario.exitCode.category == ExitCode.Category.Unverified
            }.groupBy { it.completedScenario.orchestratableScenario.baseFile },
            errored = completedScenariosWithMetadata.filter {
                it.completedScenario.error != null ||
                    it.completedScenario.exitCode.category == ExitCode.Category.Error
            }.groupBy { it.completedScenario.orchestratableScenario.baseFile },
            verified = completedScenariosWithMetadata.filter {
                it.completedScenario.exitCode.category == ExitCode.Category.Ok
            }.groupBy { it.completedScenario.orchestratableScenario.baseFile },
        )
    }

    data class CompletedScenarioWithMetadata<RC : ScenarioResultContext>(
        val id: String,
        val script: URI?,
        val report: URI?,
        val completedScenario: CompletedScenario<GuidedScenario, RC>,
        val fields: Map<String, String>
    ) {
        val errorText by lazy { completedScenario.error?.message ?: "" }
    }

    class ReportModel<RC : ScenarioResultContext>(
        val additionalFields: List<String>,
        val incomplete: Map<File, List<CompletedScenarioWithMetadata<RC>>>,
        val errored: Map<File, List<CompletedScenarioWithMetadata<RC>>>,
        val unverified: Map<File, List<CompletedScenarioWithMetadata<RC>>>,
        val verified: Map<File, List<CompletedScenarioWithMetadata<RC>>>,
    ) {
        val baseScenarios =
            incomplete.keys + errored.keys + unverified.keys + verified.keys
    }

    private fun createHtmlReport(model: ReportModel<RC>) =
        createHTML(xhtmlCompatible = true).html {
            attributes["data-theme"] = "vdm"
            head {
                meta(charset = "UTF-8")
                link(rel = "stylesheet", type = "text/css", href = "vdm.css")
                title(content = "Verification summary Report")
            }
            body {
                h1 { +"Summary" }
                table {
                    tr {
                        th { +"Verified" }
                        td { +"${model.verified.values.flatten().size}" }
                    }
                    tr {
                        th { +"Unverified" }
                        td { +"${model.unverified.values.flatten().size}" }
                    }
                    tr {
                        th { +"Errored" }
                        td { +"${model.errored.values.flatten().size}" }
                    }
                    tr {
                        th { +"Incomplete" }
                        td { +"${model.incomplete.values.flatten().size}" }
                    }
                }
                if (model.baseScenarios.size > 1) {
                    h1 { +"Breakdown by base scenario" }
                    table {
                        tr {
                            th { +"Scenario" }
                            th { +"Verified" }
                            th { +"Unverified" }
                            th { +"Errored" }
                            th { +"Incomplete" }
                            th { +"Failed dependency" }
                        }
                        model.baseScenarios.sorted().forEach { f ->
                            tr {
                                td { +f.name }
                                td { +"${model.verified[f]?.size ?: 0}" }
                                td { +"${model.unverified[f]?.size ?: 0}" }
                                td { +"${model.errored[f]?.size ?: 0}" }
                                td { +"${model.incomplete[f]?.size ?: 0}" }
                            }
                        }
                    }
                }
                h1 { +"Scenario list" }
                table {
                    tr {
                        th { +"Scenario" }
                        th { +"Status" }
                        model.additionalFields.forEach {
                            th { +it }
                        }
                        th { +"Error" }
                    }
                    scenarioListRows(model.additionalFields, model.unverified, "Unverified")
                    scenarioListRows(model.additionalFields, model.errored, "Errored")
                    scenarioListRows(model.additionalFields, model.incomplete, "Incomplete")
                    scenarioListRows(model.additionalFields, model.verified, "Verified")
                }
            }
        }

    private fun TABLE.scenarioListRows(
        additionalFields: List<String>,
        runs: Map<File, List<CompletedScenarioWithMetadata<RC>>>,
        status: String
    ) {
        runs.values.flatten().sortedBy { it.completedScenario.orchestratableScenario.baseFile.name }.forEach { cswm ->
            tr {
                td {
                    if (cswm.script == null) {
                        +cswm.id
                    } else {
                        a(href = cswm.script.toString()) {
                            +cswm.id
                        }
                    }
                }
                td {
                    if (cswm.report == null) {
                        +status
                    } else {
                        a(href = cswm.report.toString()) {
                            +status
                        }
                    }
                }
                additionalFields.forEach {
                    td { +(cswm.fields[it] ?: "") }
                }
                td { +cswm.errorText }
            }
        }
    }

    companion object {
        val Log = LoggerFactory.getLogger(ProgressReportGenerator::class.java)
    }
}
