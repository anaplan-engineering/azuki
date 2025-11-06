package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery

interface TicTacToeQueryFactory : QueryFactory {

    fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>,
        deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ): DerivedQuery<T>

    fun <T, C : Collection<T>> createForSomeQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>,
        deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ): DerivedQuery<T>

    fun getGames() : Query<List<String>> = UnsupportedQuery()
    fun getWidth(gameName: String) : Query<Int> = UnsupportedQuery()
    fun getHeight(gameName: String) : Query<Int> = UnsupportedQuery()
    fun getPositions(gameName: String) : Query<List<Position>> = UnsupportedQuery()
    fun getToken(gameName: String, position: Position) : Query<String?> = UnsupportedQuery()
}
