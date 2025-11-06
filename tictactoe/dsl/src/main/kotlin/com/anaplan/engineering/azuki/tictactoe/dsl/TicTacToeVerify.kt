package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Verify
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

class TicTacToeVerify(private val queryFactory: TicTacToeQueryFactory) : Verify<TicTacToeQueryFactory> {

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    fun <T, C : Collection<T>> forAll(
        getDriver: DerivedQueryBlock.() -> (qf: TicTacToeQueryFactory) -> Query<C>, v: TicTacToeVerify.(T) -> Unit
    ) {
        derivedQueriesList.add(queryFactory.createForAllQuery(DerivedQueryBlock().getDriver()) { t: T, qf: TicTacToeQueryFactory ->
            TicTacToeVerify(qf).apply { v(t) }.queriesList
        })
    }

    fun gameHasToken(gameName: String, position: Position) {
        queriesList.add(queryFactory.getToken(gameName, position))
    }
}
