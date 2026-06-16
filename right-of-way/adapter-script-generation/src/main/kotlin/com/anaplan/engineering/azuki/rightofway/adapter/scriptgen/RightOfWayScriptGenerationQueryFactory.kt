package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.DerivedQuery
import com.anaplan.engineering.azuki.core.system.Query
import com.anaplan.engineering.azuki.core.system.UnsupportedQuery
import com.anaplan.engineering.azuki.core.system.unsupportedBehavior
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDerivedQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQuery
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationQueryWithDummy
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.RightOfWayQueryFactory
import com.anaplan.engineering.azuki.rightofway.dsl.DerivedQueryBlock
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayQueries
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayVerify
import kotlin.reflect.KFunction

/**
 * Root class for the three types of query factory (queries in query position, queries in verification position,
 * and queries in derived position).
 */
abstract class AbstractRightOfWayScriptGenerationQueryFactory() : RightOfWayQueryFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T, C : Collection<T>> createForAllQuery(
        derivedFrom: (RightOfWayQueryFactory) -> Query<C>, deriveQuery: (T, RightOfWayQueryFactory) -> DerivedQuery<*>
    ): DerivedQuery<T> {
        // TODO: is there any way to avoid using a dummy here?
        val genQueryWithDummy =
            derivedFrom(RightOfWayScriptGenerationDerivedQueryFactory) as ScriptGenerationQueryWithDummy<C, T>
        val derivation = deriveQuery(genQueryWithDummy.dummy, this) as ScriptGenerationDerivedQuery<*>

        return ScriptGenerationDerivedQuery {
            """
            forAll({
                ${genQueryWithDummy.getQueryScript()}
            }, {
                ${derivation.getDerivedQueryScript()}
            })
            """
        }
    }

    override fun <T> liftQueriesToDerivedQuery(queries: List<Query<*>>): DerivedQuery<T> =
        ScriptGenerationDerivedQuery {
            // The DSL doesn't expose this function directly, it just has a variant of `forAll` that takes a query.
            queries.joinToString("\n") { (it as ScriptGenerationQuery<*>).getQueryScript() }
        }
}

abstract class RightOfWayScriptGenerationQueryFactory(val queryPosition: QueryPosition) :
    AbstractRightOfWayScriptGenerationQueryFactory() {

    override fun getPlayOrder(gameName: String) = query<List<String>>(QueryReference.PlayOrder, gameName)
    override fun getPositions(gameName: String) = query<List<Position>>(QueryReference.Positions, gameName)
    override fun getToken(gameName: String, position: Position) =
        query<String?>(QueryReference.Token, gameName, position)

    override fun canPlayerPlaceToken(gameName: String, playerName: String, position: Position) =
        query<Boolean>(QueryReference.CanPlayerPlaceToken, gameName, playerName, position)

    private fun <T> query(reference: QueryReference, vararg args: Any?) = reference.inPosition(queryPosition)?.let {
        RightOfWayScriptGenerationQuery<T> {
            RightOfWayScriptingHelper.scriptifyFunction(it, *args)
        }
    } ?: UnsupportedQuery()
}

object RightOfWayScriptGenerationQueryQueryFactory : RightOfWayScriptGenerationQueryFactory(QueryPosition.Query)
object RightOfWayScriptGenerationVerificationQueryFactory : RightOfWayScriptGenerationQueryFactory(QueryPosition.Verify)

object RightOfWayScriptGenerationDerivedQueryFactory : AbstractRightOfWayScriptGenerationQueryFactory() {

    override fun getPlayOrder(gameName: String) =
        query<List<String>, String>(QueryReference.PlayOrder, "(insert player here)", gameName)

    override fun getPositions(gameName: String) =
        query<List<Position>, Position>(QueryReference.Positions, Position(-1, -1), gameName)

    private fun <C : Collection<T>, T> query(reference: QueryReference, dummy: T, vararg args: Any?) =
        reference.inPosition(QueryPosition.Derived)?.let {
            ScriptGenerationQueryWithDummy(RightOfWayScriptGenerationQuery<C> {
                RightOfWayScriptingHelper.scriptifyFunction(it, *args)
            }, dummy)
        } ?: UnsupportedQuery()
}

enum class QueryPosition {
    Query, Verify, Derived
}

enum class QueryReference(
    val inQueryPosition: KFunction<*>, val inVerificationPosition: KFunction<*>, val inDerivedPosition: KFunction<*>?
) {
    PlayOrder(RightOfWayQueries::getPlayOrder,
        RightOfWayVerify::gameHasPlayOrder,
        DerivedQueryBlock::getPlayOrder),
    Positions(RightOfWayQueries::getPositions,
        RightOfWayVerify::gameHasPositions,
        DerivedQueryBlock::getPositions),
    Token(RightOfWayQueries::getToken,
        RightOfWayVerify::gameHasToken,
        null),
    CanPlayerPlaceToken(RightOfWayQueries::canPlayerPlaceToken, RightOfWayVerify::playerCanPlaceToken, null);

    fun inPosition(position: QueryPosition) = when (position) {
        QueryPosition.Query -> inQueryPosition
        QueryPosition.Verify -> inVerificationPosition
        QueryPosition.Derived -> inDerivedPosition
    }
}

fun interface RightOfWayScriptGenerationQuery<T> : ScriptGenerationQuery<T> {

    override val behavior get() = unsupportedBehavior
}
