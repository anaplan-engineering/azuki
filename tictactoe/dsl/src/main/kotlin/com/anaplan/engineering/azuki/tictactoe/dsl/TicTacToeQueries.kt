package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.dsl.Verify
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

class TicTacToeQueries(private val queryFactory: TicTacToeQueryFactory) : Queries<TicTacToeQueryFactory> {

    private val queriesList = mutableListOf<Query<*>>()
    private val forallQueriesList = mutableListOf<DerivedQuery<*>>()

    override fun queries() = ScenarioQueries(queriesList, forallQueriesList)
}
