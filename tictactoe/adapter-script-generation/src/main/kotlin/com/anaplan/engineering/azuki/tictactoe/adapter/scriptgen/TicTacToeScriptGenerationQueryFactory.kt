package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDerivedQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQueryWithDummy
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.dsl.DerivedQueryBlock
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeQueries
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeVerify
import kotlin.reflect.KFunction

/**
 * Root class for the three types of query factory (queries in query position, queries in verification position,
 * and queries in derived position).
 */
abstract class AbstractTicTacToeScriptGenerationQueryFactory() : TicTacToeQueryFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (TicTacToeQueryFactory) -> Query<C>, deriveQuery: (T, TicTacToeQueryFactory) -> List<Query<*>>
    ) : DerivedQuery<T> {
        val genQueryWithDummy =
            derivedFrom(TicTacToeScriptGenerationDerivedQueryFactory) as ScriptGenerationQueryWithDummy<C, T>
        val derivedFromScript = genQueryWithDummy.getQueryScript()
        val derivationScript = deriveQuery(genQueryWithDummy.dummy, this).joinToString("\n")
        { (it as ScriptGenerationQuery<*>).getQueryScript() }

        return TicTacToeScriptGenerationDerivedQuery(
            QueryOperator.ForAll,
            derivedFromScript,
            derivationScript,
        )
    }
}

abstract class TicTacToeScriptGenerationQueryFactory(val queryPosition: QueryPosition) :
    AbstractTicTacToeScriptGenerationQueryFactory() {

    override fun getGames() = query<List<String>>(QueryReference.Games)
    override fun getPlayOrder(gameName: String) = query<List<String>>(QueryReference.PlayOrder, gameName)
    override fun getPositions(gameName: String) = query<List<Position>>(QueryReference.Positions, gameName)
    override fun getWidth(gameName: String) = query<Int>(QueryReference.Width, gameName)
    override fun getHeight(gameName: String) = query<Int>(QueryReference.Height, gameName)
    override fun getToken(gameName: String, position: Position) =
        query<String?>(QueryReference.Token, gameName, position)

    override fun canPlayerPlaceToken(gameName: String, playerName: String, position: Position) =
        query<Boolean>(QueryReference.CanPlayerPlaceToken, gameName, playerName, position)

    private fun <T> query(reference: QueryReference, vararg args: Any?) = reference.inPosition(queryPosition)?.let {
        TicTacToeScriptGenerationQuery<T> {
            TicTacToeScriptingHelper.scriptifyFunction(it, *args)
        }
    } ?: UnsupportedQuery()
}

object TicTacToeScriptGenerationQueryQueryFactory : TicTacToeScriptGenerationQueryFactory(QueryPosition.Query)
object TicTacToeScriptGenerationVerificationQueryFactory : TicTacToeScriptGenerationQueryFactory(QueryPosition.Verify)

object TicTacToeScriptGenerationDerivedQueryFactory : AbstractTicTacToeScriptGenerationQueryFactory() {

    override fun getGames() = query<List<String>, String>(QueryReference.Games, "(insert game here)")
    override fun getPlayOrder(gameName: String) =
        query<List<String>, String>(QueryReference.PlayOrder, "(insert player here)", gameName)

    override fun getPositions(gameName: String) =
        query<List<Position>, Position>(QueryReference.Positions, Position(-1, -1), gameName)

    private fun <C : Collection<T>, T> query(reference: QueryReference, dummy: T, vararg args: Any?) =
        reference.inPosition(QueryPosition.Derived)?.let {
            ScriptGenerationQueryWithDummy(TicTacToeScriptGenerationQuery<C> {
                TicTacToeScriptingHelper.scriptifyFunction(it, *args)
            }, dummy)
        } ?: UnsupportedQuery()
}

enum class QueryPosition {
    Query, Verify, Derived
}

enum class QueryReference(
    val inQueryPosition: KFunction<*>, val inVerificationPosition: KFunction<*>, val inDerivedPosition: KFunction<*>?
) {
    Games(TicTacToeQueries::getGames, TicTacToeVerify::hasGames, DerivedQueryBlock::getGames), PlayOrder(
        TicTacToeQueries::getPlayOrder,
        TicTacToeVerify::gameHasPlayOrder,
        DerivedQueryBlock::getPlayOrder),
    Width(TicTacToeQueries::getWidth, TicTacToeVerify::gameHasWidth, null), Height(TicTacToeQueries::getHeight,
        TicTacToeVerify::gameHasHeight,
        null),
    Positions(TicTacToeQueries::getPositions,
        TicTacToeVerify::gameHasPositions,
        DerivedQueryBlock::getPositions),
    Token(TicTacToeQueries::getToken,
        TicTacToeVerify::gameHasToken,
        null),
    CanPlayerPlaceToken(TicTacToeQueries::canPlayerPlaceToken, TicTacToeVerify::playerCanPlaceToken, null);

    fun inPosition(position: QueryPosition) = when (position) {
        QueryPosition.Query -> inQueryPosition
        QueryPosition.Verify -> inVerificationPosition
        QueryPosition.Derived -> inDerivedPosition
    }
}

class TicTacToeScriptGenerationDerivedQuery<T>(
    val operator: QueryOperator, val derivedFromScript: String, val derivationScript: String
) : ScriptGenerationDerivedQuery<T> {

    override fun getDerivedQueryScript() = """
            ${operator.script}({
                $derivedFromScript
            }, {
                $derivationScript
            })
        """
}

enum class QueryOperator(val script: String) {
    ForAll("forAll")
}

fun interface TicTacToeScriptGenerationQuery<T> : ScriptGenerationQuery<T> {

    override val behavior get() = unsupportedBehavior
}
