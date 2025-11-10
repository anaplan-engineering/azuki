package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext
import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
import com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeBuildableScenario

object TicTacToeScenarioParser : SimpleScenarioParser<TicTacToeBuildableScenario>() {

    override val defaultImports: ScenarioParsingContext.() -> Unit = ScenarioParsingContext::addTicTacToeDefaultImports
}

fun ScenarioParsingContext.addTicTacToeDefaultImports() {
    import("com.anaplan.engineering.azuki.tictactoe.dsl.*")
    import("com.anaplan.engineering.azuki.tictactoe.*")
}
