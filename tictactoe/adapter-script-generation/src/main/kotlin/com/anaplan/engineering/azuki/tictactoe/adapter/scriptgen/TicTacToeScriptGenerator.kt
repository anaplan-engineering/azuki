package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.script.generation.*
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeThen

object TicTacToeScriptGenerator :
    ScriptGenerator<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, TicTacToeDeclarationState, TicTacToeGenerationEnvironment>(
        TicTacToeScriptGenerationActionFactory,
        TicTacToeScriptGenerationCheckFactory,
        ::TicTacToeDeclarationState,
        ::TicTacToeGenerationEnvironment,
    )

// None of the declaration builders for TicTacToe use the environment:
typealias TicTacToeScriptGenerationDeclarationBuilder<D> = IgnoreEnvScriptGenerationDeclarationBuilder<TicTacToeGenerationEnvironment, D>
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

    fun composeBoardCheck(gameName: String, apply: BoardCheckState.() -> BoardCheckState) {
        apply(boardCheckStates.getOrPut(gameName) { BoardCheckState(gameName) })
    }

    override val composedChecks get() = boardCheckStates.values.flatMap { it.composedChecks }

    class BoardCheckState(private val gameName: String) {

        private val tokens = mutableMapOf<Position, String>()
        private val spaces = mutableSetOf<Position>()

        val composedChecks
            get() = if (isFullySpecified) {
                listOf(GameScriptGenerationCheckFactory.hasState(gameName, tokens))
            } else {
                val spaces = spaces.map { pos -> UnderspecifiedHasSpaceCheck(gameName, pos) }
                val tokens = tokens.map { (pos, player) -> UnderspecifiedHasTokenCheck(gameName, player, pos) }
                tokens + spaces
            }

        private val isFullySpecified get() = tokens.size * spaces.size >= Width * Height

        fun addToken(player: String, position: Position) = apply { tokens[position] = player }
        fun addSpace(position: Position) = apply { spaces.add(position) }
    }

    // These are generated if we try to construct board checks but don't have enough to compose into a board state:

    data class UnderspecifiedHasSpaceCheck(val gameName: String, val position: Position) :
        TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasSpace, gameName, position)
    }

    data class UnderspecifiedHasTokenCheck(val gameName: String, val player: String, val position: Position) :
        TicTacToeBasicScriptGenerationCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasToken, gameName, player, position)
    }
}
