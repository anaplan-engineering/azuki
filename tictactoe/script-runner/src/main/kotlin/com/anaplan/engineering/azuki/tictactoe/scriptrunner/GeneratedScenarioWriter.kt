package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerator
import java.io.File

class GeneratedScenarioWriter(private val scenarioName: String, private val outputDir: File) {

    fun writeGeneratedScenario(result: OracleScenarioResult) {
        val generatedScenario = result.generatedScenario ?: return
        writeOracleScenario(generatedScenario)

        val queryingOracle = result.oracleResults.lastOrNull { it.hasTask(TaskType.Query) }

        if (queryingOracle == null) {
            writeIncompleteScenario(generatedScenario)
            return
        }

        @Suppress("UNCHECKED_CAST") val answers =
            queryingOracle.findTask(TaskType.Query)?.result as? List<Answer<*, TicTacToeCheckFactory>> ?: return
        if (answers.isNotEmpty()) {
            writeVerifiableScenario(generatedScenario, answers)
        }
    }

    private fun writeVerifiableScenario(
        generatedScenario: OracleScenario<TicTacToeActionFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>,
        answers: List<Answer<*, TicTacToeCheckFactory>>
    ) {
        val gen = TicTacToeScriptGenerator()

        val script = """
            ${gen.generateGivenScript(generatedScenario)}
            ${gen.generateWheneverScript(generatedScenario)}
            ${gen.generateThenScript(answers)}
        """.trimIndent()
        File(outputDir, "$scenarioName-gen-vfy.scn").writeText(script)
    }

    private fun writeIncompleteScenario(
        generatedScenario: OracleScenario<TicTacToeActionFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>
    ) {
        val gen = TicTacToeScriptGenerator()

        val script = """
            ${gen.generateGivenScript(generatedScenario)}
            ${gen.generateWheneverScript(generatedScenario)}
        """.trimIndent()
        File(outputDir, "$scenarioName-gen-err.scn").writeText(script)
    }

    private fun writeOracleScenario(generatedScenario: OracleScenario<TicTacToeActionFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory>) {
        val gen = TicTacToeScriptGenerator()

        val script = gen.generateScript(generatedScenario)
        File(outputDir, "$scenarioName-gen-ocl.scn").writeText(script)
    }

}
