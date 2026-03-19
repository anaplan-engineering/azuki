package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.*
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeRunnableScenario
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

val TicTacToeScriptGeneration = ScriptGenerationService.new(TicTacToeScriptGenerationActionFactory,
    TicTacToeScriptGenerationCheckFactory,
    ::TicTacToeDeclarationState).withEnvironmentFactory(::TicTacToeGenerationEnvironment)
    .withActionGeneratorFactory(TicTacToeScriptGenerationActionGeneratorFactory)
    .withQueryFactory(TicTacToeScriptGenerationQueryQueryFactory)
    .withVerifyFactory(TicTacToeScriptGenerationVerificationQueryFactory).build()

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

    val boardCheckComposers = BoardCheckComposerRegistry()

    /**
     * Holds state for composing game board checks, keyed on game names.
     *
     * We want to collapse individual board-has-X checks into a single board-has-state check,
     * but only if the entire board is covered by them.  This class allows this by storing
     * composers for these checks.
     */
    class BoardCheckComposerRegistry :
        CheckComposerRegistry<TicTacToeGenerationEnvironment, String, BoardCheckComposer> by CheckComposerMap(::BoardCheckComposer) {

        fun hasToken(gameName: String, player: String, position: Position) =
            onKey(gameName).tryRegister { hasToken(player, position) }

        fun hasSpace(gameName: String, position: Position) = onKey(gameName).tryRegister { hasSpace(position) }
    }

    /**
     * Holds state for composing board checks for one game.
     *
     * Once all positions on a given game's board are fully specified, this composer can
     * be composed into a single board check asserting all tokens and spaces.
     */
    class BoardCheckComposer(private val gameName: String) : CheckComposer<TicTacToeGenerationEnvironment> {

        private val tokens = mutableMapOf<Position, String>()
        private val spaces = mutableSetOf<Position>()
        private val isFullySpecified get() = tokens.size + spaces.size == Width * Height

        override fun compose(environment: TicTacToeGenerationEnvironment) = if (isFullySpecified) {
            success(listOf(GameScriptGenerationCheckFactory.hasState(gameName, tokens)))
        } else {
            failure(IllegalStateException("board has not been fully specified"))
        }

        fun hasToken(player: String, position: Position) = at(position) { tokens[position] = player }
        fun hasSpace(position: Position) = at(position) { spaces.add(position) }

        private fun at(position: Position, fn: BoardCheckComposer.() -> Unit) =
            if (position in tokens || position in spaces) {
                failure(IllegalStateException("position $position is checked already"))
            } else success(apply(fn))
    }
}

object TicTacToeRunnableScenarioClassGenerator : RunnableScenarioClassGenerator<TicTacToeRunnableScenario>(
    ticTacToeStandardImports.toList(),
    TicTacToeRunnableScenario::class)

/**
 * Default imports that should be added to any tic-tac-toe script (generated or parsed).
 */
val ticTacToeStandardImports =
    arrayOf("com.anaplan.engineering.azuki.tictactoe.dsl.*", "com.anaplan.engineering.azuki.tictactoe.*")
