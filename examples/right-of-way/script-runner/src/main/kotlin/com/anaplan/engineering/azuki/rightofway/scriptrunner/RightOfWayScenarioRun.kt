package com.anaplan.engineering.azuki.rightofway.scriptrunner

data class RightOfWayScenarioRun(
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
