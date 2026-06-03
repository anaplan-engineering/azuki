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

    override fun getAirspaces(): Query<List<String>> = query(
        value = { env ->
            env.airspaceManager.activeAirspaces.toList()
        },
        checks = { airSpaces -> airSpaces.isNotEmpty() }
    )
//    override fun getAircrafts(airspaceName: String): Query<Aircrafts> = UnsupportedQuery()
//    override fun getVelocities(airspaceName: String): Query<Map<String,Velocity>> = UnsupportedQuery()
//    override fun getPositions(airspaceName: String): Query<Map<String,Position>> = UnsupportedQuery()
//    override fun hasRightOfWay(airspaceName: String): Query<Aircrafts> = UnsupportedQuery()

//    override fun getAircraft(airspaceName: String) = query(
//        value = { env ->
//            env.withAirspace(airspaceName) {
//                this.
//        //    .map { it.token.symbol }
//            }
//        },
//        checks = { playOrder -> { listOf(game.hasPlayOrder(airspaceName, playOrder)) } })

//    override fun getPositions(gameName: String) = query(value = { env ->
//        env.withGame(gameName) { (1..height).flatMap { row -> (1..width).map { col -> Position(row, col) } } }
//    }, checks = {
//        // This query is only intended as a driver for for-all quantifiers, so we don't support checking it
//        { emptyList() }
//    })
//
//    override fun getToken(gameName: String, position: Position) =
//        query(value = { env -> env.withGame(gameName) { tokenAt(position)?.symbol } }, checks = { token ->
//            token?.let { { listOf(game.hasToken(gameName, it, position)) } } ?: {
//                listOf(game.hasSpace(gameName, position))
//            }
//        })
//
//    override fun canPlayerPlaceToken(gameName: String, playerName: String, position: Position) = query(value = { env ->
//        env.withGame(gameName) { canMove(playerName, position) }
//    }, checks = { canPlace ->
//        { listOf(player.canPlaceToken(gameName, playerName, position, canPlace)) }
//    })
}

private fun <T> query(
    value: (ExecutionEnvironment) -> T,
    checks: (T) -> RightOfWayCheckFactory.() -> List<Check>
): Query<T> = object : RunnableQuery<ExecutionEnvironment, RightOfWayCheckFactory, T> {

    override val behavior: Behavior get() = unsupportedBehavior
    override fun run(environment: ExecutionEnvironment) = value(environment).let { SampleAnswer(this, it, checks(it)) }
}
