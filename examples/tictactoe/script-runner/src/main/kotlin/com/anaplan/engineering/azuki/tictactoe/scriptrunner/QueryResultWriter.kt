package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.File

class QueryResultWriter(private val queryResultDir: File, private val queryResultFileName: String) {

    fun writeQueryResults(scenarioName: String, results: OracleScenarioResult) {
        val oracleResult = results.oracleResults.lastOrNull()
        val queryTask = oracleResult?.findTask(TaskType.Query)
        if (queryTask?.result == null) {
            Log.debug("No query results for scenario=$scenarioName")
            return
        }
        @Suppress("UNCHECKED_CAST") val answers = queryTask.result as List<Answer<*, TicTacToeCheckFactory>>
        val queryResults = QueryResults(answers.groupBy { it.to }.map { (q, a) ->
            val queryAnswers = a.flatMap { it.value as? Collection<*> ?: listOf(it.value) }
            QueryResult(q.behavior, q.toString(), queryAnswers.map { it?.toString() ?: "null" })
        })
        val queryResultsFile = File(queryResultDir, queryResultFileName)
        queryResultsFile.writeText(objectMapper.writeValueAsString(queryResults))
    }

    companion object {

        private val Log = LoggerFactory.getLogger(QueryResultWriter::class.java)
        private val objectMapper = jacksonObjectMapper()
    }
}
