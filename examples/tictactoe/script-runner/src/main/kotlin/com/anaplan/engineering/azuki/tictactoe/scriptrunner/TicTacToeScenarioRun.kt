package com.anaplan.engineering.azuki.tictactoe.scriptrunner

data class TicTacToeScenarioRun(
    val result: Result,
    val duration: Long,
    val errorText: String?,
    val testFile: String?,
) {

    // Incomplete is a 'good' state that typically means that some pre-condition has not been satisfied
    enum class Result {
        Incomplete, Errored, Verified, Unverified,
    }
}
