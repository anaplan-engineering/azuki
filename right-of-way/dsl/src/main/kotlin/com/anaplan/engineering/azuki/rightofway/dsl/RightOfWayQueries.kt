package com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory

class RightOfWayQueries(private val queryFactory: RightOfWayQueryFactory) : Queries<RightOfWayQueryFactory> {

    fun allAircraftsIn(airspaceName: String) = addQuery { allAircraftsIn(airspaceName) }
    fun airspaceHasAircraft(airspaceName: String, aircraftName: String) =
        addQuery { airspaceHasAircraft(airspaceName, aircraftName) }
    fun hasRightOfWay(airspaceName: String) = addQuery { hasRightOfWay(airspaceName) }
    fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) =
        addQuery { hasRightOfWay(airspaceName, aircraft0, aircraft1) }


    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    private fun addQuery(via: RightOfWayQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}

class DerivedQueryBlock {

    private fun <T> derived(f: RightOfWayQueryFactory.() -> Query<T>) = f
}
