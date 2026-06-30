package com.anaplan.engineering.azuki.rightofway.scriptrunner

import com.anaplan.engineering.azuki.core.system.Behavior

data class QueryResults(val results: List<QueryResult>)

data class QueryResult(val queryBehaviour: Behavior, val queryDescription: String, val answers: List<String>)
