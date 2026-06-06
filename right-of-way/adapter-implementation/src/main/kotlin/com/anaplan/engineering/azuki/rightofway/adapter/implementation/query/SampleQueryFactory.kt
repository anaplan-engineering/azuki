package com.anaplan.engineering.azuki.rightofway.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment

class SampleQueryFactory : RightOfWayQueryFactory {

    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (RightOfWayQueryFactory) -> Query<C>, deriveQuery: (T, RightOfWayQueryFactory) -> DerivedQuery<*>
    ) = ForAllRunnableDerivedQuery(derivedFrom(this).ensureRunnable()) { t ->
        @Suppress("UNCHECKED_CAST") (deriveQuery(t,
            this) as RunnableDerivedQuery<ExecutionEnvironment, RightOfWayCheckFactory, *>)
    }

    override fun <T> liftQueriesToDerivedQuery(queries: List<Query<*>>) =
        RunnableQueriesAsDerivedQuery.fromQueries<ExecutionEnvironment, RightOfWayCheckFactory, T>(queries)

    override fun getAircrafts(airspaceName: String) = query(
        value = { env -> env.withAirspace(airspaceName) { aircraftIds.toSet() } },
        // this checks param is curried: first parameter is a List<Aircraft> and second is the RightOfWayCheckFactory
        // This query is only intended as a driver for for-all quantifiers, so we don't support checking it
        checks = { { emptyList() } })

    // TODO not sure this is the intention for queries (i.e. more general for all aircraft in airspace)
    //      or more specific, for individual aircraft
    override fun hasRightOfWay(airspaceName: String) = query(
        value = { env ->
            env.withAirspace(airspaceName) {
                // { (x, y) in pairs(aircraftIds) | hasRightOfWay(x, y) }
                aircraftIds.flatMap { x ->
                    (aircraftIds - x).map { y -> x to y }
                }.filter { (x, y) ->
                    hasRightOfWay(getAircraft(x), getAircraft(y))
                }.toSet()
            }
        },
        checks = { pairs ->
            {
                pairs.map { (x, y) ->
                    airspace.hasRightOfWay(airspaceName, x, y)
                }
            }
        }
    )
}

private fun <T> query(
    value: (ExecutionEnvironment) -> T,
    checks: (T) -> RightOfWayCheckFactory.() -> List<Check>
): Query<T> = object : RunnableQuery<ExecutionEnvironment, RightOfWayCheckFactory, T> {

    override val behavior: Behavior get() = unsupportedBehavior
    override fun run(environment: ExecutionEnvironment) = value(environment).let { SampleAnswer(this, it, checks(it)) }
}
