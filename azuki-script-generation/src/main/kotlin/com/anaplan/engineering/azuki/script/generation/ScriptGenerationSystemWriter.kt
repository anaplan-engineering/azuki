package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.SystemDefinition
import com.anaplan.engineering.azuki.core.system.SystemWriter
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Writes intermediate system definitions as partial scenarios using a scenario generation service.
 */
abstract class ScriptGenerationSystemWriter<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory> :
    SystemWriter<AF, CF, QF, AGF> {

    protected abstract val scriptGeneration: ScriptGenerationService<AF, CF, QF, QF, AGF, *, *>
    protected abstract val classGeneration: RunnableScenarioClassGenerator<*>
    protected abstract val outputDir: File

    private val scenarioDir by lazy {
        File(outputDir, "scenarios").apply { mkdirs() }
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
        scriptGeneration.given {
            fromActions(systemDefinition.declarations)
        }.whenever {
            fromActions(systemDefinition.commands)
        }.then {
            fromChecks(systemDefinition.checks)
        }.verifiableScenario.write(context ?: "scenario-vfy", ScenarioSuffix)
    }

    private fun writeOracleScenario(systemDefinition: SystemDefinition, context: String?) {
        val oracle = scriptGeneration.given {
            fromActions(systemDefinition.declarations)
        }.generateBlocks {
            // System definition action generators go after 'given' iff there are no commands
            if (systemDefinition.commands.isEmpty()) block {
                fromActionGenerators(systemDefinition.actionGenerators)
            }
        }.whenever {
            fromActions(systemDefinition.commands)
        }.generateBlocks {
            // System definition action generators go after 'whenever' iff there are commands
            if (systemDefinition.commands.isNotEmpty()) block {
                fromActionGenerators(systemDefinition.actionGenerators)
            }
        }.verify {
            fromQueries(systemDefinition.queries)
            fromDerivedQueries(systemDefinition.forAllQueries)
        }

        oracle.oracleScenario.write(context ?: "scenario-ocl", ScenarioSuffix)

        if (systemDefinition.actionGenerators.isNotEmpty()) {
            val scenarioScript = oracle.takeGivenAndWhenever().then {
                fromChecks(listOf(checkFactory.systemValid()))
            }.verifiableScenario

            val testCase = classGeneration.generate(className = context ?: "scenario-ocl",
                packageName = "debug",
                scenarioScript = scenarioScript,
                implementationVersions = emptyMap())
            val definition = ScenarioFormatter.formatScenario(testCase.definition)
            write(testCase.className, TestSuffix, definition)
        }
    }

    private fun writeQueryScenario(systemDefinition: SystemDefinition, context: String?) {
        scriptGeneration.given {
            fromActions(systemDefinition.declarations)
        }.whenever {
            fromActions(systemDefinition.commands)
        }.query {
            fromQueries(systemDefinition.queries)
            fromDerivedQueries(systemDefinition.forAllQueries)
        }.queryScenario.write(context ?: "scenario-ocl", ScenarioSuffix)
    }

    private fun ScenarioScript.write(prefix: String, suffix: String) {
        write(prefix, suffix, render {
            indentLevel = 0
            formatter = Formatter.Full
            scriptType = ScriptType.Standalone
        })
    }

    private fun write(prefix: String, suffix: String, text: String) {
        File(scenarioDir, "$prefix.$suffix").apply { writeText(text) }
    }

    companion object {

        private const val ScenarioSuffix = "scn"
        private const val TestSuffix = "kt"
        private val Log = LoggerFactory.getLogger(ScriptGenerationSystemWriter::class.java)
    }
}
