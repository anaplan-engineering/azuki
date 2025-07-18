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
    ScriptGenerator<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, TicTacToeDeclarationState, TicTacToeCheckState>(
        TicTacToeScriptGenActionFactory,
        TicTacToeScriptGenCheckFactory,
        TicTacToeDeclarationState.Factory,
        ::TicTacToeCheckState)

internal const val Width = 3
internal const val Height = 3

val TicTacToeScriptingHelper = ScriptingHelper(mapOf(
    String::class to { v: Any? -> "\"\"\"${v.toString()}\"\"\"" },
    Position::class to { v: Any? -> (v as Position).let { "${it.row} to ${it.col}" } },
    Int::class to { v: Any? -> v.toString() },
    Long::class to { v: Any? -> v.toString() },
))

class TicTacToeCheckState : AbstractScriptGenerationCheckState() {

    // We want to collapse individual board-has-X checks into a single board-has-state check,
    // but only if the entire board is covered by them.
    private val boards: MutableMap<String, Board> = mutableMapOf()

    fun addToken(gameName: String, player: String, position: Position) {
        boards.merge(gameName, Board(tokens = mapOf(position to player), spaces = emptySet()), Board::plus)
    }

    fun addSpace(gameName: String, position: Position) {
        boards.merge(gameName, Board(tokens = emptyMap(), spaces = setOf(position)), Board::plus)
    }

    override fun getChecks() = finishedChecks + getBoardChecks()

    private fun getBoardChecks() = boards.flatMap { (gameName, board) ->
        if (board.isFullySpecified) {
            listOf(GameScriptGenCheckFactory.hasState(gameName, board.tokens))
        } else {
            val spaces = board.spaces.map { pos -> UnderspecifiedHasSpaceCheck(gameName, pos) }
            val tokens = board.tokens.map { (pos, player) -> UnderspecifiedHasTokenCheck(gameName, player, pos) }
            tokens + spaces
        }
    }

    private data class Board(val tokens: Map<Position, String>, val spaces: Set<Position>) {
        val isFullySpecified get() = tokens.size * spaces.size >= Width * Height

        operator fun plus(other: Board) = Board(tokens + other.tokens, spaces + other.spaces)
    }

    // These are generated if we try to construct board checks but don't have enough to compose into a board state:

    data class UnderspecifiedHasSpaceCheck(val gameName: String, val position: Position) : ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasSpace, gameName, position)
    }

    data class UnderspecifiedHasTokenCheck(val gameName: String, val player: String, val position: Position) :
        ScriptGenCheck() {

        override fun getCheckScript() =
            TicTacToeScriptingHelper.scriptifyFunction(TicTacToeThen::boardHasToken, gameName, player, position)
    }
}
