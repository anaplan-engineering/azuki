package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.*
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

val TicTacToeScriptGeneration = ScriptGenerationService.create(TicTacToeScriptGenerationActionFactory,
    TicTacToeScriptGenerationCheckFactory,
    ::TicTacToeDeclarationState).withEnvironmentFactory(::TicTacToeGenerationEnvironment)

class TicTacToeScriptGenerator(environment: TicTacToeGenerationEnvironment = TicTacToeGenerationEnvironment()) :
    VerificationCapableScriptGenerator<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory, TicTacToeDeclarationState, TicTacToeGenerationEnvironment>(
        TicTacToeScriptGenerationActionFactory,
        TicTacToeScriptGenerationCheckFactory,
        ::TicTacToeDeclarationState,
        environment,
        TicTacToeScriptGenerationActionGeneratorFactory,
        TicTacToeScriptGenerationQueryQueryFactory,
        TicTacToeScriptGenerationVerificationQueryFactory,
    )

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
