package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

class TicTacToeQueries(private val queryFactory: TicTacToeQueryFactory) : Queries<TicTacToeQueryFactory> {

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    fun getGames() = addQuery { getGames() }
    fun getWidth(gameName: String) = addQuery { getWidth(gameName) }
    fun getHeight(gameName: String) = addQuery { getHeight(gameName) }
    fun getPositions(gameName: String) = addQuery { getPositions(gameName) }
    fun getToken(gameName: String, position: Position) = addQuery { getToken(gameName, position) }

    private fun addQuery(via: TicTacToeQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}

class DerivedQueryBlock {

    fun getGames() = derived { getGames() }
    fun getWidth(gameName: String) = derived { getWidth(gameName) }
    fun getHeight(gameName: String) = derived { getHeight(gameName) }
    fun getPositions(gameName: String) = derived { getPositions(gameName) }

    private fun <T> derived(f: TicTacToeQueryFactory.() -> Query<T>) = f
}
