package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.system.Behavior

open class QueryResults(val results: List<QueryResult>)

data class QueryResult(val queryBehaviour: Behavior, val queryDescription: String, val answers: List<String>)
