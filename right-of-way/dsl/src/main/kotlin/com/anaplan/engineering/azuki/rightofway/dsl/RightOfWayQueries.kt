package com.anaplan.engineering.azuki.rightofway.dsl

import com.anaplan.engineering.azuki.core.dsl.Queries
import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory

class RightOfWayQueries(private val queryFactory: RightOfWayQueryFactory) : Queries<RightOfWayQueryFactory> {

    fun allAircraftsIn(airspaceName: String) =
        addQuery { allAircraftsIn(airspaceName) }
    fun airspaceHasAircraft(airspaceName: String, aircraftName: String) =
        addQuery { airspaceHasAircraft(airspaceName, aircraftName) }
    fun hasRightOfWay(airspaceName: String) =
        addQuery { hasRightOfWay(airspaceName) }
    fun hasRightOfWay(airspaceName: String, withRightOfWay: String, givingWay: String) =
        addQuery { hasRightOfWay(airspaceName, withRightOfWay, givingWay) }

    override fun queries() = ScenarioQueries(queriesList, derivedQueriesList)

    private val queriesList = mutableListOf<Query<*>>()
    private val derivedQueriesList = mutableListOf<DerivedQuery<*>>()

    private fun addQuery(via: RightOfWayQueryFactory.() -> Query<*>) {
        queriesList.add(queryFactory.via())
    }
}

class DerivedQueryBlock {

    fun allAircraftsIn(airspaceName: String) =
        derived { allAircraftsIn(airspaceName) }
    fun airspaceHasAircraft(airspaceName: String, aircraftName: String) =
        derived { airspaceHasAircraft(airspaceName, aircraftName) }
    fun hasRightOfWay(airspaceName: String) =
        derived { hasRightOfWay(airspaceName) }
    fun hasRightOfWay(airspaceName: String, withRightOfWay: String, givingWay: String) =
        derived { hasRightOfWay(airspaceName, withRightOfWay, givingWay) }

    private fun <T> derived(f: RightOfWayQueryFactory.() -> Query<T>) = f
}
