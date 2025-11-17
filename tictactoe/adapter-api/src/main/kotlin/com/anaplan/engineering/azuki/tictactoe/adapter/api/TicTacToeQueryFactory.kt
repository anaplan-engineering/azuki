package com.anaplan.engineering.azuki.tictactoe.adapter.api

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery

interface TicTacToeQueryFactory : QueryFactory {

    /**
     * Creates a for-all query.
     * The body is another derived query, to allow nesting;
     * use `liftQueriesToDerivedQuery` to adapt the final set of (non-derived) queries to the right type.
     */
    fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> DerivedQuery<*>
    ): DerivedQuery<T>

    /**
     * Lifts a list of queries to a derived query.
     */
    fun <T> liftQueriesToDerivedQuery(queries: List<Query<*>>): DerivedQuery<T>

    fun getPlayOrder(gameName: String): Query<List<String>> = UnsupportedQuery()
    fun getPositions(gameName: String): Query<List<Position>> = UnsupportedQuery()
    fun getToken(gameName: String, position: Position): Query<String?> = UnsupportedQuery()
    fun canPlayerPlaceToken(gameName: String, playerName: String, position: Position): Query<Boolean> =
        UnsupportedQuery()
}
