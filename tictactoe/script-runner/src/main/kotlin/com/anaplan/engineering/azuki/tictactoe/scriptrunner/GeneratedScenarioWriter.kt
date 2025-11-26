package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.runner.TaskType
import com.anaplan.engineering.azuki.core.system.Answer
import com.anaplan.engineering.azuki.script.generation.ScenarioScript
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGeneration
import java.io.File

class GeneratedScenarioWriter(private val scenarioName: String, private val outputDir: File) {

    fun writeGeneratedScenario(result: OracleScenarioResult) {
        val generatedScenario = result.generatedScenario ?: return
        val oracleBuilder = TicTacToeScriptGeneration.scenario.oracleBuilder(generatedScenario)

        val oracleScenario = oracleBuilder.oracleScenario()
        oracleScenario.write("$scenarioName-gen-ocl.scn")

        // We can reuse these blocks from the oracle as the first blocks of the other scenario outputs:
        val verifiableScenarioBuilder = oracleBuilder.takeGivenAndWhenever()

        val queryingOracle = result.oracleResults.lastOrNull { it.hasTask(TaskType.Query) }
        val taskResult = queryingOracle?.findTask(TaskType.Query)?.result
        val answers = (taskResult as? List<*>)?.filterIsInstance<Answer<*, TicTacToeCheckFactory>>()

        if (queryingOracle == null) {
            verifiableScenarioBuilder.incompleteScenario().write("$scenarioName-gen-err.scn") {
                // This isn't a full verifiable scenario, so don't present it as one
                inOuterBlock = false
            }
        } else if (!answers.isNullOrEmpty()) {
            verifiableScenarioBuilder.then {
                fromAnswers(answers)
            }.verifiableScenario().write("$scenarioName-gen-vfy.scn")
        }
    }

    private fun ScenarioScript.write(fileName: String, renderOpts: ScenarioScript.Renderer.() -> Unit = {}) {
        File(outputDir, fileName).writeText(render(renderOpts))
    }
}
