package com.anaplan.engineering.azuki.tictactoe.dsl

import com.anaplan.engineering.azuki.core.dsl.Verify
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory

class TicTacToeVerify(private val queryFactory: TicTacToeQueryFactory) : Verify<TicTacToeQueryFactory> {

    fun <T, C : Collection<T>> forAll(
        getDriver: DerivedQueryBlock.() -> (qf: TicTacToeQueryFactory) -> Query<C>, v: TicTacToeVerify.(T) -> Unit
    ) {
        derivedQueriesList.add(queryFactory.createForAllQuery(DerivedQueryBlock().getDriver()) { t: T, qf: TicTacToeQueryFactory ->
            val verify = TicTacToeVerify(qf).apply { v(t) }

            // TODO: relax these requirements by creating a derived query concatenation operator?
            require(verify.queriesList.isEmpty() || verify.derivedQueriesList.isEmpty()) { "forAll cannot have both derived and non-derived queries" }
            require(verify.derivedQueriesList.size <= 1) { "forAll cannot have more than one nested derived query" }

            if (verify.queriesList.isNotEmpty()) {
                qf.liftQueriesToDerivedQuery<T>(verify.queriesList)
            } else {
                verify.derivedQueriesList[0]
            }
        })
    }

    fun hasGames() = addQuery { getGames() }
    fun gameHasPlayOrder(gameName: String) = addQuery { getPlayOrder(gameName) }
    fun gameHasWidth(gameName: String) = addQuery { getWidth(gameName) }
    fun gameHasHeight(gameName: String) = addQuery { getHeight(gameName) }
    fun gameHasPositions(gameName: String) = addQuery { getPositions(gameName) }
    fun gameHasToken(gameName: String, position: Pair<Int, Int>) = addQuery { getToken(gameName, Position(position)) }
    fun playerCanPlaceToken(gameName: String, playerName: String, position: Pair<Int, Int>) =
        addQuery { canPlayerPlaceToken(gameName, playerName, Position(position)) }

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    private fun addQuery(via: TicTacToeQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}
