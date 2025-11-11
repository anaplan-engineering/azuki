package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

class TicTacToeQueries(private val queryFactory: TicTacToeQueryFactory) : Queries<TicTacToeQueryFactory> {

    fun getGames() = addQuery { getGames() }
    fun getPlayOrder(gameName: String) = addQuery { getPlayOrder(gameName) }
    fun getWidth(gameName: String) = addQuery { getWidth(gameName) }
    fun getHeight(gameName: String) = addQuery { getHeight(gameName) }
    fun getPositions(gameName: String) = addQuery { getPositions(gameName) }
    fun getToken(gameName: String, position: Pair<Int, Int>) = addQuery { getToken(gameName, Position(position)) }
    fun canPlayerPlaceToken(gameName: String, playerName: String, position: Pair<Int, Int>) =
        addQuery { canPlayerPlaceToken(gameName, playerName, Position(position)) }

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    private fun addQuery(via: TicTacToeQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}

class DerivedQueryBlock {

    fun getGames() = derived { getGames() }
    fun getPlayOrder(gameName: String) = derived { getPlayOrder(gameName) }
    fun getPositions(gameName: String) = derived { getPositions(gameName) }

    private fun <T> derived(f: TicTacToeQueryFactory.() -> Query<T>) = f
}
