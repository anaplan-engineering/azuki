package com.anaplan.engineering.azuki.resultreporter

import com.anaplan.engineering.azuki.core.runner.JUnitScenarioResult
import com.anaplan.engineering.azuki.core.runner.JUnitScenarioType
import com.anaplan.engineering.azuki.core.runner.ScenarioResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File

class ScenarioReportGenerator(
    val sourceFiles: List<File>,
    val reportDir: File
) {

    private val objectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    fun generate() {
        val results = sourceFiles.flatMap {
            objectMapper.readValue<List<ScenarioResult>>(it)
        }
        val model = buildModel(results)
        writeReport(model)
    }

    companion object {
        const val ReportTitle = "Azuki Scenario Results"
    }

    private fun buildModel(results: List<ScenarioResult>): Model {
        val (standardResults, persistenceResults) =
            results.partition { it.persistenceImplementationName == null }
        return Model(
            buildResultModel(standardResults, false),
            buildResultModel(persistenceResults, true)
        )
    }

    private fun String?.version() = if (this == null) "" else " ($this)"

    private fun buildResultModel(results: List<ScenarioResult>, isPersistence: Boolean): ResultModel =
        results.groupBy {
            if (isPersistence) {
                "${it.implementationName}${it.implementationVersion.version()} -> ${it.persistenceImplementationName}${it.persistenceImplementationVersion.version()}"
            } else {
                "${it.implementationName}${it.implementationVersion.version()}"
            }
        }.entries.associate { (impl, r) ->
            impl to r.groupBy { it.scenarioType }.entries.associate { (type, s) ->
                type to s.groupBy { it.state }.entries.associate { (state, t) ->
                    state to t.size
                }
            }
        }


    data class Model(
        val standard: ResultModel,
        val persistence: ResultModel
    )

    private fun writeReport(model: Model) {
        File(reportDir, "index.htm").writeText(StringBuilder().appendHTML().html {
            head {
                meta(charset = "UTF-8")
                title { +ReportTitle }
            }
            body {
                h1 { +ReportTitle }
                if (model.standard.isNotEmpty()) {
                    h2 { +"Standard results" }
                    addResultTable(model.standard)
                }
                if (model.persistence.isNotEmpty()) {
                    h2 { +"Persistence verification results" }
                    addResultTable(model.persistence)
                }
            }
        }.toString())
    }


    // nedd to merge cells etc
    private fun BODY.addResultTable(resultModel: ResultModel) {
        table {
            tr {
                th { +"Implementation" }
                th { +"Scenario type" }
                // TODO group by category
                JUnitScenarioResult.values().forEach {
                    th { +it.name }
                }
            }
            resultModel.entries.forEach { (impl, r) ->
                r.entries.forEach { (type, counts) ->
                    tr {
                        td { +impl }
                        td { +type.name }
                        val total = counts.values.sum()
                        JUnitScenarioResult.values().forEach {
                            val count = counts[it] ?: 0
                            val pc = if (count == 0) "" else " (${(count * 100) / total}%)"
                            td { +"$count$pc" }
                        }
                    }
                }
            }
            tr {
                th { +"Total"}
                val totals = mutableMapOf<JUnitScenarioResult, Int>()
                resultModel.entries.forEach { (_, r) ->
                    r.entries.forEach { (_, counts) ->
                        counts.forEach { (result, count) ->
                            totals[result] = (totals[result] ?: 0) + count
                        }
                    }
                }
                td { +"-" } // TODO might want totals by type
                val grandTotal = totals.values.sum()
                JUnitScenarioResult.values().forEach {
                    val count = totals[it] ?: 0
                    val pc = if (count == 0) "" else " (${(count * 100) / grandTotal}%)"
                    td { +"$count$pc" }
                }
            }
        }
    }

}

typealias ResultModel = Map<String, Map<JUnitScenarioType, Map<JUnitScenarioResult, Int>>>
