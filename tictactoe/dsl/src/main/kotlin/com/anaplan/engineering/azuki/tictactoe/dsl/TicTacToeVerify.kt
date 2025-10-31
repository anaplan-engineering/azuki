package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Verify
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

class TicTacToeVerify(private val queryFactory: TicTacToeQueryFactory) : Verify<TicTacToeQueryFactory> {

    private val queriesList = mutableListOf<Query<*>>()
    private val forallQueriesList = mutableListOf<DerivedQuery<*>>()

    override fun queries() = ScenarioQueries(queriesList, forallQueriesList)
}
