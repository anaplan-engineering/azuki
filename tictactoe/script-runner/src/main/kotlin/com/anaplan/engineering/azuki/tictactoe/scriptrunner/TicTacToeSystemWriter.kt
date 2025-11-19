package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.scenario.ScenarioQueries
import com.anaplan.engineering.azuki.core.system.SystemDefinition
import com.anaplan.engineering.azuki.core.system.SystemWriter
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import com.anaplan.engineering.azuki.script.generation.FullScenarioScript
import com.anaplan.engineering.azuki.script.generation.toScriptGenAction
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGeneration
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationQueryQueryFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerator
import org.slf4j.LoggerFactory
import java.io.File

class TicTacToeSystemWriter :
    SystemWriter<TicTacToeActionFactory, TicTacToeCheckFactory, TicTacToeQueryFactory, TicTacToeActionGeneratorFactory> {

    override val actionFactory = TicTacToeScriptGenerationActionFactory
    override val actionGeneratorFactory = TicTacToeScriptGenerationActionGeneratorFactory
    override val checkFactory = TicTacToeScriptGenerationCheckFactory
    override val queryFactory = TicTacToeScriptGenerationQueryQueryFactory

    private val scenarioDir by lazy {
        File(Command.outputDir, "scenarios").apply { mkdirs() }
    }

    override fun write(systemDefinition: SystemDefinition, context: String?) {
        if (systemDefinition.checks.isNotEmpty()) {
            writeVerifiableScenario(systemDefinition, context)
        } else if (systemDefinition.actionGenerators.isNotEmpty()) {
            writeOracleScenario(systemDefinition, context)
        } else if (systemDefinition.forAllQueries.isNotEmpty() || systemDefinition.queries.isNotEmpty()) {
            writeQueryScenario(systemDefinition, context)
        } else {
            Log.error("Unable to write system definition: $systemDefinition")
        }
    }

    private fun writeVerifiableScenario(systemDefinition: SystemDefinition, context: String?) {
        TicTacToeScriptGeneration.given {
            fromSystemDefinition(systemDefinition)
        }.whenever {
            fromSystemDefinition(systemDefinition)
        }.then {
            fromSystemDefinition(systemDefinition)
        }.verifiableScenario().write(context ?: "scenario-vfy", ScenarioSuffix)
    }

    private fun writeOracleScenario(systemDefinition: SystemDefinition, context: String?) {
        val oracle = TicTacToeScriptGeneration.given {
            fromSystemDefinition(systemDefinition)
        }.generate {
            blocksFromSystemDefinition(systemDefinition)
        }.whenever {
            fromSystemDefinition(systemDefinition)
        }.generate {
            blocksFromSystemDefinition(systemDefinition)
        }.verify {
            fromSystemDefinition(systemDefinition)
        }

        oracle.oracleScenario().write(context ?: "scenario-ocl", ScenarioSuffix)

        if (systemDefinition.actionGenerators.isNotEmpty()) {
            val scenarioScript = oracle.takeGivenAndWhenever().then {
                fromChecks(listOf(checkFactory.systemValid()))
            }.verifiableScenario().renderUnformatted(indent = 2)

            val testCase = TicTacToeRunnableScenarioClassGenerator.generate(className = context ?: "scenario-ocl",
                packageName = "debug",
                scenarioScript = scenarioScript,
                implementationVersions = emptyMap())
            val definition = ScenarioFormatter.formatScenario(testCase.definition)
            write(testCase.className, TestSuffix, definition)
        }
    }

    private fun writeQueryScenario(systemDefinition: SystemDefinition, context: String?) {
        val gen = TicTacToeScriptGenerator()
        write(context ?: "scenario-ocl",
            ScenarioSuffix,
            gen.generateQueryScenarioScript(
                gen.generateGivenScriptFromActions(systemDefinition.declarations),
                gen.generateWheneverScriptFromActions(systemDefinition.commands.map { it.toScriptGenAction() }),
                gen.generateQueryScriptFromQueries(ScenarioQueries(systemDefinition.queries,
                    systemDefinition.forAllQueries)),
            ))
    }

    private fun FullScenarioScript.write(prefix: String, suffix: String) {
        write(prefix, suffix, renderFormatted())
    }

    private fun write(prefix: String, suffix: String, text: String) {
        File(scenarioDir, "$prefix.$suffix").apply { writeText(text) }
    }

    companion object {

        private const val ScenarioSuffix = "scn"
        private const val TestSuffix = "kt"
        private val Log = LoggerFactory.getLogger(TicTacToeSystemWriter::class.java)
    }
}
