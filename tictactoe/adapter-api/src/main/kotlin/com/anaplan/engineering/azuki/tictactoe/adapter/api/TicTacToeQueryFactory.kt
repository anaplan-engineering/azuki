package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery

interface TicTacToeQueryFactory : QueryFactory {

    fun getToken(gameName: String, position: Position) : Query<String?> = UnsupportedQuery()
}
