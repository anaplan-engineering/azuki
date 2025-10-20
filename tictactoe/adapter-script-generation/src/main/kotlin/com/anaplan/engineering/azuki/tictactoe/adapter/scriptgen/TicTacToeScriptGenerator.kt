package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.script.generation.*
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState

object TicTacToeScriptGenerator :
    ScriptGenerator<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, TicTacToeDeclarationState, TicTacToeGenerationEnvironment>(
        TicTacToeScriptGenerationActionFactory,
        TicTacToeScriptGenerationCheckFactory,
        ::TicTacToeDeclarationState,
        ::TicTacToeGenerationEnvironment,
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
))

class TicTacToeGenerationEnvironment : ScriptGenerationEnvironment {

    // We want to collapse individual board-has-X checks into a single board-has-state check,
    // but only if the entire board is covered by them.
    private val boardCheckStates: MutableMap<String, BoardCheckState> = mutableMapOf()

    fun composeBoardCheck(
        gameName: String, decomposeTo: TicTacToeScriptGenerationCheck, apply: BoardCheckState.() -> BoardCheckState
    ): ComposedCheckResolver<TicTacToeGenerationEnvironment> {
        apply(boardCheckStates.getOrPut(gameName) { BoardCheckState(gameName) })
        return ComposedCheckResolver { it.resolveComposedBoardCheck(gameName, listOf(decomposeTo)) }
    }

    private fun resolveComposedBoardCheck(gameName: String, decomposeTo: List<TicTacToeScriptGenerationCheck>) =
        boardCheckStates[gameName].let { it?.resolveComposedCheck(decomposeTo) ?: decomposeTo }

    class BoardCheckState(private val gameName: String) {

        private val tokens = mutableMapOf<Position, String>()
        private val spaces = mutableSetOf<Position>()
        private var isTaken = false

        fun resolveComposedCheck(decomposeTo: List<TicTacToeScriptGenerationCheck>) = if (isTaken) {
            emptyList()
        } else if (isFullySpecified) {
            // only allow the composed form to appear in the final script once
            isTaken = true
            listOf(GameScriptGenerationCheckFactory.hasState(gameName, tokens))
        } else {
            // if we're not fully specified, return the original check every time
            decomposeTo
        }

        private val isFullySpecified get() = tokens.size * spaces.size >= Width * Height

        fun addToken(player: String, position: Position) = apply { tokens[position] = player }
        fun addSpace(position: Position) = apply { spaces.add(position) }
    }
}
