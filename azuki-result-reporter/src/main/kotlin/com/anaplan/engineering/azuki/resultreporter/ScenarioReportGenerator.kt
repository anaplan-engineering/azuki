package com.anaplan.engineering.azuki.resultreporter

import com.anaplan.engineering.azuki.core.runner.JUnitScenarioResult
import com.anaplan.engineering.azuki.core.runner.JUnitScenarioType
import com.anaplan.engineering.azuki.core.runner.ScenarioResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File

class ScenarioReportGenerator(
    val sourceFiles: List<File>,
    val reportDir: File
) {

    private val objectMapper by lazy { jacksonObjectMapper() }

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
        ResultModel(
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
            },
            JUnitScenarioResult.values().filter { resultType ->
                results.any { it.state == resultType }
            }
        )


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

    data class ResultModel(
        val results: Map<String, Map<JUnitScenarioType, Map<JUnitScenarioResult, Int>>>,
        val resultTypes: List<JUnitScenarioResult>
    ) {
        fun isNotEmpty() = results.isNotEmpty()

        val categoryCounts = JUnitScenarioResult.Category.values().associateWith { category ->
            resultTypes.count { category == it.category }
        }.filter { (_, v) -> v != 0 }

    }

    private fun BODY.addResultTable(resultModel: ResultModel) {

        val cellStyle = "border: 1px solid black; border-collapse: collapse;padding-left: 5px;padding-right: 5px;"

        fun TR.ths(block : TH.() -> Unit = {}) {
            th {
                style = "$cellStyle;text-align: left;background-color:#eeeeee"
                block()
            }
        }

        fun TR.tds(block : TD.() -> Unit = {}) {
            td {
                style = cellStyle
                block()
            }
        }

        fun TR.tdt(block : TD.() -> Unit = {}) {
            td {
                style = "$cellStyle;font-style: italic;background-color:#eeeeee"
                block()
            }
        }

        fun TR.addCounts(counts: Map<JUnitScenarioResult, Int>, isTotal: Boolean = false) {
            val total = counts.values.sum()
            resultModel.resultTypes.forEach {
                val count = counts[it] ?: 0
                val pc = if (count == 0) "" else " (${(count * 100) / total}%)"
                val text = "$count$pc"
                if (isTotal) {
                    tdt { +text }
                } else {
                    tds { +text }
                }
            }
        }

        table {
            style = "border: 1px solid black; border-collapse: collapse;"
            tr {
                ths {
                    rowSpan = "2"
                    +"Implementation"
                }
                ths {
                    rowSpan = "2"
                    +"Scenario type"
                }
                resultModel.categoryCounts.forEach { (cat, count) ->
                    ths {
                        colSpan = "$count"
                        +cat.name
                    }
                }
            }
            tr {
                resultModel.resultTypes.forEach {
                    ths { +it.name }
                }
            }
            resultModel.results.entries.sortedBy { it.key }.forEach { (impl, r) ->
                val sortedResults = r.entries.sortedBy { it.key }
                sortedResults.first().let { (type, counts) ->
                    tr {
                        ths {
                            rowSpan = "${r.size + 1}"
                            +impl
                        }
                        tds { +type.name }
                        addCounts(counts)
                    }
                }
                sortedResults.drop(1).forEach { (type, counts) ->
                    tr {
                        tds { +type.name }
                        addCounts(counts)
                    }
                }
                val totals = mutableMapOf<JUnitScenarioResult, Int>()
                    r.entries.forEach { (_, counts) ->
                        counts.forEach { (result, count) ->
                            totals[result] = (totals[result] ?: 0) + count
                        }
                    }
                tr {
                    tdt { +"Total" }
                    addCounts(totals, isTotal = true)
                }
            }
            tr {
                tdt { +"Total" }
                val totals = mutableMapOf<JUnitScenarioResult, Int>()
                resultModel.results.entries.forEach { (_, r) ->
                    r.entries.forEach { (_, counts) ->
                        counts.forEach { (result, count) ->
                            totals[result] = (totals[result] ?: 0) + count
                        }
                    }
                }
                tdt { +"-" }
                addCounts(totals, isTotal = true)
            }
        }
    }

}


