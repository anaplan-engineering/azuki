package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.engineering.azuki.core.system.SystemDefinition
import com.anaplan.engineering.azuki.core.system.SystemWriter
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import com.anaplan.engineering.azuki.script.generation.ScenarioScript
import com.anaplan.engineering.azuki.tictactoe.adapter.api.*
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeRunnableScenarioClassGenerator
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGeneration
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationActionFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationActionGeneratorFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationCheckFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen.TicTacToeScriptGenerationQueryQueryFactory
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
            fromActions(systemDefinition.declarations)
        }.whenever {
            fromActions(systemDefinition.commands)
        }.then {
            fromChecks(systemDefinition.checks)
        }.verifiableScenario().write(context ?: "scenario-vfy", ScenarioSuffix)
    }

    private fun writeOracleScenario(systemDefinition: SystemDefinition, context: String?) {
        val oracle = TicTacToeScriptGeneration.given {
            fromActions(systemDefinition.declarations)
        }.generate {
            // System definition action generators go after 'given' iff there are no commands
            if (systemDefinition.commands.isEmpty()) block {
                fromActionGenerators(systemDefinition.actionGenerators)
            }
        }.whenever {
            fromActions(systemDefinition.commands)
        }.generate {
            // System definition action generators go after 'whenever' iff there are commands
            if (systemDefinition.commands.isNotEmpty()) block {
                fromActionGenerators(systemDefinition.actionGenerators)
            }
        }.verify {
            fromQueries(systemDefinition.queries)
            fromDerivedQueries(systemDefinition.forAllQueries)
        }

        oracle.oracleScenario().write(context ?: "scenario-ocl", ScenarioSuffix)

        if (systemDefinition.actionGenerators.isNotEmpty()) {
            val scenarioScript = oracle.takeGivenAndWhenever().then {
                fromChecks(listOf(checkFactory.systemValid()))
            }.verifiableScenario().render {
                indent = 2
                inOuterBlock = true

                // We're going to format the whole test-case anyway, so formatting twice is pointless
                useFormatter = false
            }

            val testCase = TicTacToeRunnableScenarioClassGenerator.generate(className = context ?: "scenario-ocl",
                packageName = "debug",
                scenarioScript = scenarioScript,
                implementationVersions = emptyMap())
            val definition = ScenarioFormatter.formatScenario(testCase.definition)
            write(testCase.className, TestSuffix, definition)
        }
    }

    private fun writeQueryScenario(systemDefinition: SystemDefinition, context: String?) {
        TicTacToeScriptGeneration.given {
            fromActions(systemDefinition.declarations)
        }.whenever {
            fromActions(systemDefinition.commands)
        }.query {
            fromQueries(systemDefinition.queries)
            fromDerivedQueries(systemDefinition.forAllQueries)
        }.queryScenario().write(context ?: "scenario-ocl", ScenarioSuffix)
    }

    private fun ScenarioScript.write(prefix: String, suffix: String) {
        write(prefix, suffix, render {
            indent = 0
            useFormatter = true
            inOuterBlock = true
        })
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
