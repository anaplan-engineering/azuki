package com.anaplan.engineering.azuki.rightofway.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.*
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayCheckFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.api.toAircraft
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

    // all aircrafts in airspace
    override fun allAircraftsIn(airspaceName: String) = query(
        value = { env ->
            env.withAirspace(airspaceName) {
                // Better idiom to avoid intermediate mappings/pairs; given sticks to map's original domain, associate with it
                //aircraftIds.map { name -> name to getAircraft(name).toAircraft() }.toMap()
                //aircraftIds.associate { name -> name to getAircraft(name).toAircraft() }
                aircraftIds.associateWith { name -> getAircraft(name).toAircraft() }
            }
                },
        // this checks param is curried: first parameter is a List<Aircraft> and inner one is a
        // RightOfWayCheckFactory (receiver) lambda to a List<Check> (!)
        // This query is only intended as a driver for for-all quantifiers, so we don't support checking it
        checks = { x -> {
            //println(this) //  RightOfWayCheckFactory.(): List<Check>
            emptyList<Check>() } })

    override fun airspaceHasAircraft(airspaceName: String, aircraftName: String) = query(
        value = { env ->
            env.withAirspace(airspaceName) {
                hasAircraft(aircraftName)
            }
        },
        checks = {
            { listOf(airspace.hasAircraft(airspaceName, aircraftName)) }
        }
    )

    // TODO LF: not sure this is the intention for queries (i.e. more general for all aircraft in airspace)
    //      or more specific, for individual aircraft
    // all aircrafts with right of way pairwise in airspace
    override fun hasRightOfWay(airspaceName: String) = query(
        value = { env ->
            // return a map of Aircrafts with right of way
            env.withAirspace(airspaceName) {
                // { (x, y) | x in set aircraftIds, y in set aircraftIds \ {x} & hasRightOfWay(x, y) }
                aircraftIds.flatMap { x ->
                    (aircraftIds - x).map { y -> x to y }
                }.filter { (x, y) ->
                    hasRightOfWay(getAircraft(x), getAircraft(y))
                    //}.map { (x, y) -> getAircraft(x) to getAircraft(y) }.toMap()  // Map<Aircraft, Aircraft>
                    //}.associate { (x, y) -> getAircraft(x) to getAircraft(y) } // Map<Aircraft, Aircraft> faster
                    //.mapTo(HashSet()) { (x, y) -> getAircraft(x) to getAircraft(y) } // Set<Pair<Aircraft, Aircraft>>
                }.toSet() // Set<Pair<String, String>>
            }
        },
        checks = { pairs ->
            {
                pairs.map { (x, y) ->
                    //TODO LF: should these be aircraft types instead of strings?
                    airspace.hasRightOfWay(airspaceName, x, y)
                }
            }
        }
    )

    // given aircrafts have right of way in airspace
    override fun hasRightOfWay(airspaceName: String, aircraft0: String, aircraft1: String) = query(
        value = { env ->
            env.withAirspace(airspaceName) {
                hasAircraft(aircraft0) && hasAircraft(aircraft1)
                    //&& hasRightOfWay(getAircraft(aircraft0), getAircraft(aircraft1))
            }
        },
        checks = { result ->
            {
                //TODO LF: not sure this is the intended behavior
                // if the aircraft aren't in the airspace there is nothing to check; there is otherwise
                // or should only be for when `hasRightOfWay`?
                if (!result)
                    /*return@query*/ emptyList()
                else
                    // if aircraft exist, the query is about whether they have right of way
                    listOf(airspace.hasRightOfWay(airspaceName, aircraft0, aircraft1))
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
