package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.system.NoActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.NoQueryFactory
import com.anaplan.engineering.azuki.script.generation.ScriptGenerator
import com.anaplan.engineering.azuki.script.generation.ScriptingHelper
import com.anaplan.engineering.azuki.script.generation.SimpleScriptGenerationCheckState
import com.anaplan.engineering.azuki.tictactoe.adapter.api.MoveMap
import com.anaplan.engineering.azuki.tictactoe.adapter.api.Position
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.TicTacToeDeclarationState

object TicTacToeScriptGenerator :
    ScriptGenerator<TicTacToeActionFactory, TicTacToeCheckFactory, NoQueryFactory, NoActionGeneratorFactory, TicTacToeDeclarationState, SimpleScriptGenerationCheckState>(
        TicTacToeScriptGenActionFactory,
        TicTacToeScriptGenCheckFactory,
        TicTacToeDeclarationState.Factory,
        ::SimpleScriptGenerationCheckState)

val TicTacToeScriptingHelper = ScriptingHelper(mapOf(
    String::class to { v: Any? -> "\"\"\"${v.toString()}\"\"\"" },
    Position::class to { v: Any? -> (v as Position).let { "${it.row} to ${it.col}" } },
    Int::class to { v: Any? -> v.toString() },
    Long::class to { v: Any? -> v.toString() },
))

fun MoveMap.toAscii(): String {
    val sb = StringBuilder()

    (1..3).forEach { row ->
        if (row != 1) {
            sb.append('\n')
        }

        (1..3).forEach { col ->
            if (col != 1) {
                sb.append(" | ")
            }

            val pos = Position(row, col)
            val marker = getOrDefault(pos, defaultValue = ".")

            sb.append(marker)
        }
    }

    return sb.toString()
}
