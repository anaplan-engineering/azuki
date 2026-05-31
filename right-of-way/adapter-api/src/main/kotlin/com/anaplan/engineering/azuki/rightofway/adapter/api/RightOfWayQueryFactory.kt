package com.anaplan.engineering.azuki.rightofway.adapter.api

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery

interface RightOfWayQueryFactory : QueryFactory {

    /**
     * Creates a for-all query.
     * The body is another derived query, to allow nesting;
     * use `liftQueriesToDerivedQuery` to adapt the final set of (non-derived) queries to the right type.
     */
    fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (RightOfWayQueryFactory) -> Query<C>, deriveQuery: (T, RightOfWayQueryFactory) -> DerivedQuery<*>
    ): DerivedQuery<T>

    /**
     * Lifts a list of queries to a derived query.
     */
    fun <T> liftQueriesToDerivedQuery(queries: List<Query<*>>): DerivedQuery<T>

    fun getVelocities(aircraftName: String): Query<List<Velocity>> = UnsupportedQuery()
    fun getPositions(aircraftName: String): Query<List<Position>> = UnsupportedQuery()
    fun hasRightOfWay(aircraftName: String): Query<Boolean> = UnsupportedQuery()
}
