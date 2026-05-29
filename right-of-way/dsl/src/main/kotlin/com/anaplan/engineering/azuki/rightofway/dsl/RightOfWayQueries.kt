package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory

class RightOfWayQueries(private val queryFactory: RightOfWayQueryFactory) : Queries<RightOfWayQueryFactory> {

    fun getPlayOrder(gameName: String) = addQuery { getPlayOrder(gameName) }
    fun getPositions(gameName: String) = addQuery { getPositions(gameName) }
    fun getToken(gameName: String, position: Pair<Int, Int>) = addQuery { getToken(gameName, Position(position)) }
    fun canPlayerPlaceToken(gameName: String, playerName: String, position: Pair<Int, Int>) =
        addQuery { canPlayerPlaceToken(gameName, playerName, Position(position)) }

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    private fun addQuery(via: RightOfWayQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}

class DerivedQueryBlock {

    fun getPlayOrder(gameName: String) = derived { getPlayOrder(gameName) }
    fun getPositions(gameName: String) = derived { getPositions(gameName) }

    private fun <T> derived(f: RightOfWayQueryFactory.() -> Query<T>) = f
}
