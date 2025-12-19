package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.api.TicTacToeQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGeneration
import com.anaplan.engineering.azuki.verify.generation.JUnitTestCaseWriter
import java.io.File

class TicTacToeJUnitTestCaseWriter(
    verifiedTestsDir: File, unverifiedTestsDir: File, generatedTestPackage: String, generatedTestClass: String?
) : JUnitTestCaseWriter<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>(
    verifiedTestsDir,
    unverifiedTestsDir,
    generatedTestPackage,
    generatedTestClass,
) {

    override val generateScript = TicTacToeScriptGeneration
    override val generateRunnable = TicTacToeRunnableScenarioClassGenerator
}
