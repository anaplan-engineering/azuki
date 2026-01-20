package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.script.generation.*
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.script.generation.runnable.IdentifierMapper
import com.anaplan.engineering.azuki.script.generation.runnable.Importable
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier
import com.anaplan.engineering.azuki.script.generation.runnable.RunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeBehaviours
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeFunctionalElements
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeRunnableScenario
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.reflect.KClass

val TicTacToeScriptGeneration = ScriptGenerationService.new(TicTacToeScriptGenerationActionFactory,
    TicTacToeScriptGenerationCheckFactory,
    ::TicTacToeDeclarationState).withEnvironmentFactory(::TicTacToeGenerationEnvironment)
    .withActionGeneratorFactory(TicTacToeScriptGenerationActionGeneratorFactory)
    .withQueryFactory(TicTacToeScriptGenerationQueryQueryFactory)
    .withVerifyFactory(TicTacToeScriptGenerationVerificationQueryFactory).build()

// None of the declaration builders for TicTacToe use the environment:
typealias TicTacToeScriptGenerationDeclarationBuilder<D> = ScriptGenerationDeclarationBuilder<TicTacToeGenerationEnvironment, D>
typealias TicTacToeScriptGenerationDeclarationBuilderFactory<D> = ScriptGenerationDeclarationBuilderFactory<TicTacToeGenerationEnvironment, D>

internal const val Width = 3
internal const val Height = 3

val TicTacToeScriptingHelper = ScriptingHelper(mapOf(
    String::class to { v: Any? -> "\"\"\"${v.toString()}\"\"\"" },
    Position::class to { v: Any? -> (v as Position).let { "${it.row} to ${it.col}" } },
    Int::class to { v: Any? -> v.toString() },
    Long::class to { v: Any? -> v.toString() },
    IntRange::class to { v: Any? -> (v as IntRange).let { "(${it.first} .. ${it.last})" } },
))

class TicTacToeGenerationEnvironment : ScriptGenerationEnvironment {

    // We want to collapse individual board-has-X checks into a single board-has-state check,
    // but only if the entire board is covered by them.
    val boardCheckStates = CheckComposerMap(::BoardCheckState)

    class BoardCheckState(private val gameName: String) : CheckComposer<TicTacToeGenerationEnvironment> {

        private val tokens = mutableMapOf<Position, String>()
        private val spaces = mutableSetOf<Position>()

        override fun compose(environment: TicTacToeGenerationEnvironment) = if (isFullySpecified) {
            success(listOf(GameScriptGenerationCheckFactory.hasState(gameName, tokens)))
        } else {
            failure(IllegalStateException("board has not been fully specified"))
        }

        private val isFullySpecified get() = tokens.size + spaces.size == Width * Height

        fun hasToken(player: String, position: Position) = at(position) { tokens[position] = player }
        fun hasSpace(position: Position) = at(position) { spaces.add(position) }

        private fun at(position: Position, fn: BoardCheckState.() -> Unit) =
            if (position in tokens || position in spaces) {
                failure(IllegalStateException("position $position is checked already"))
            } else success(apply(fn))
    }
}

object TicTacToeRunnableScenarioClassGenerator : RunnableScenarioClassGenerator(ticTacToeStandardImports.toList(),
    TicTacToeRunnableScenario::class.toQualifiedIdentifier(),
    TicTacToeIdentifierMapper)

/**
 * Maps tic-tac-toe behavioral and FE constants back to their identifiers in TicTacToeBehaviours and
 * TicTacToeFunctionalElements respectively.
 */
object TicTacToeIdentifierMapper : IdentifierMapper {

    // TODO: work out how to derive this automatically

    private fun make(category: KClass<*>, element: String?) = element?.let { category.toQualifiedIdentifier() dot it }

    override fun getBehaviorIdentifier(beh: Behavior) = make(TicTacToeBehaviours::class, when (beh) {
        1 -> "NewGame"
        2 -> "PlayerMoveCount"
        3 -> "PlaceToken"
        4 -> "CreatePlayOrder"
        5 -> "GetPlayOrder"
        6 -> "GameEnd"
        7 -> "PlayerWon"
        8 -> "PlayerLost"
        9 -> "GameDrawn"
        else -> null
    })

    override fun getFunctionalElementIdentifier(fe: FunctionalElement) =
        make(TicTacToeFunctionalElements::class, when (fe) {
            1 -> "Game"
            else -> null
        })
}

/**
 * Default imports that should be added to any tic-tac-toe script (generated or parsed).
 */
val ticTacToeStandardImports = listOf(
    Importable.wildcard("com.anaplan.engineering.azuki.tictactoe"),
    Importable.wildcard("com.anaplan.engineering.azuki.tictactoe.dsl"),
)
