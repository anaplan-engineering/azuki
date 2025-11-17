package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.RunnableDerivedQuery
import com.anaplan.engineering.azuki.core.system.RunnableQuery
import com.anaplan.engineering.azuki.core.system.ForallRunnableDerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.ListRunnableDerivedQuery
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.core.system.ensureRunnable
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.api.PlayerCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.canMove
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.tokenAt

class SampleQueryFactory : TicTacToeQueryFactory {

    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> DerivedQuery<*>
    ) = ForallRunnableDerivedQuery(derivedFrom(this).ensureRunnable()) { t ->
        @Suppress("UNCHECKED_CAST") (deriveQuery(t,
            this) as RunnableDerivedQuery<ExecutionEnvironment, TicTacToeCheckFactory, *>)
    }

    override fun <T> liftQueriesToDerivedQuery(queries: List<Query<*>>) =
        ListRunnableDerivedQuery.fromQueries<ExecutionEnvironment, TicTacToeCheckFactory, T>(queries)

    override fun getGames() = query(value = { env -> env.gameManager.activeGames.toList() })

    override fun getPlayOrder(gameName: String) = query(value = { env ->
        env.withGame(gameName) { playOrder.map { it.token.symbol } }
    }, checks = { playOrder -> { listOf(game.hasPlayOrder(gameName, playOrder)) } })

    override fun getWidth(gameName: String) = query(value = { env -> env.withGame(gameName) { width } })

    override fun getHeight(gameName: String) = query(value = { env -> env.withGame(gameName) { height } })

    override fun getPositions(gameName: String) = query(value = { env ->
        env.withGame(gameName) { (1..height).flatMap { row -> (1..width).map { col -> Position(row, col) } } }
    })

    override fun getToken(gameName: String, position: Position) =
        query(value = { env -> env.withGame(gameName) { tokenAt(position)?.symbol } }, checks = { token ->
            token?.let { { listOf(game.hasToken(gameName, it, position)) } } ?: {
                listOf(game.hasSpace(gameName, position))
            }
        })

    override fun canPlayerPlaceToken(gameName: String, playerName: String, position: Position) = query(value = { env ->
        env.withGame(gameName) { canMove(playerName, position) }
    }, checks = { canPlace ->
        val check = if (canPlace) PlayerCheckFactory::canPlaceToken else PlayerCheckFactory::cannotPlaceToken
        { listOf(check(player, gameName, playerName, position)) }
    })
}

private fun <T> query(
    value: (ExecutionEnvironment) -> T,
    checks: (T) -> TicTacToeCheckFactory.() -> List<Check> = { { listOf(UnsupportedCheck) } },
): Query<T> = object : RunnableQuery<ExecutionEnvironment, TicTacToeCheckFactory, T> {

    override val behavior: Behavior get() = unsupportedBehavior
    override fun run(environment: ExecutionEnvironment) = value(environment).let { SampleAnswer(this, it, checks(it)) }
}
