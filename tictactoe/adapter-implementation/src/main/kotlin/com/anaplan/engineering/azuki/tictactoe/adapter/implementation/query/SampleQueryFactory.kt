package com.anaplan.engineering.azuki.tictactoe.adapter.implementation.query

import com.anaplan.engineering.azuki.core.system.Check
import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedCheck
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.ExecutionEnvironment
import com.anaplan.engineering.azuki.tictactoe.adapter.implementation.toPlayer
import org.slf4j.LoggerFactory

class SampleQueryFactory : TicTacToeQueryFactory {

    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ) = SampleDerivedQuery(derivedFrom(this)) { t -> deriveQuery(t, this) }

    override fun getGames() = query(value = { env -> env.gameManager.activeGames.toList() })

    override fun getPlayOrder(gameName: String) = query(value = { env ->
        env.withGame(gameName) { playOrder.map { it.token.symbol } }
    }, checks = { cf, playOrder -> listOf(cf.game.hasPlayOrder(gameName, playOrder)) })

    override fun getWidth(gameName: String) = query(value = { env -> env.withGame(gameName) { width } })

    override fun getHeight(gameName: String) = query(value = { env -> env.withGame(gameName) { height } })

    override fun getPositions(gameName: String) = query(value = { env ->
        env.withGame(gameName) {
            (1..height).flatMap { row -> (1..width).map { col -> Position(row, col) } }
        }
    })

    override fun getToken(gameName: String, position: Position) =
        query(value = { env -> env.withGame(gameName) { tokenAt(x = position.col - 1, y = position.row - 1)?.symbol } },
            checks = { cf, token ->
                listOf(if (token == null) {
                    cf.game.hasSpace(gameName, position)
                } else {
                    cf.game.hasToken(gameName, playerName = token, position)
                })
            })

    override fun canPlayerPlaceToken(gameName: String, playerName: String, position: Position) = query(value = { env ->
        env.withGame(gameName) {
            canMove(toPlayer(playerName), position.col - 1, position.row - 1)
        }
    }, checks = { cf, canPlace ->
        listOf(if (canPlace) {
            cf.player.canPlaceToken(gameName, playerName, position)
        } else {
            cf.player.cannotPlaceToken(gameName, playerName, position)
        })
    })
}

private fun <T> query(
    value: (ExecutionEnvironment) -> T,
    checks: (TicTacToeCheckFactory, T) -> List<Check> = { _, _ -> listOf(UnsupportedCheck) }
): Query<T> = object : SampleQuery<T> {

    override fun run(env: ExecutionEnvironment): SampleAnswer<T> {
        val value = value(env)

        return SampleAnswer(this, value) { checks(it, value) }
    }
}

fun interface SampleQuery<T> : Query<T> {

    override val behavior get() = unsupportedBehavior

    fun run(env: ExecutionEnvironment): SampleAnswer<T>
}

class SampleDerivedQuery<T, C : Collection<T>>(
    val driver: Query<C>, val derivedQueryFactory: (T) -> List<Query<*>>
) : DerivedQuery<T> {

    // TODO - shouldn't be filtering out unsupported queries here -- should be detecting earlier
    fun derive(env: ExecutionEnvironment) = when (driver) {
        is UnsupportedQuery -> {
            Log.error("Driver is unsupported in derived query")
            emptyList()
        }
        // TODO -- this should be different for for some.. don't map every value
        is SampleQuery<C> -> driver.run(env).value.flatMap { t ->
            derivedQueryFactory(t).mapNotNull {
                when (it) {
                    is UnsupportedQuery<*> -> {
                        Log.warn("Derived query is unsupported")
                        null
                    }

                    is SampleQuery<*> -> it
                    else -> throw IllegalStateException("derived query is incorrect class: ${it::class.simpleName}")
                }
            }
        }

        else -> throw IllegalStateException("driver query is incorrect class: ${driver::class.simpleName}")
    }

    companion object {

        private val Log = LoggerFactory.getLogger(SampleDerivedQuery::class.java)
    }
}
