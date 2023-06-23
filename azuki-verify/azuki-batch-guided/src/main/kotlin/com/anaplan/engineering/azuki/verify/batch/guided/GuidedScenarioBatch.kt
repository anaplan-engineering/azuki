package com.anaplan.engineering.azuki.verify.batch.guided

import com.anaplan.engineering.azuki.core.parser.ScenarioParser
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.verify.batch.api.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.util.*
import javax.script.ScriptException

class GuidedScenarioBatch<S : BuildableScenario<*>, RC : ScenarioResultContext>:
    OrchestratableScenarioBatch<GuidedScenario, RC> {

    private val scenarioParser: ScenarioParser<S> by lazy {
        val loader = ServiceLoader.load(ScenarioParser::class.java)
        val parsers = loader.iterator().asSequence().toList()
        if (parsers.size != 1) throw IllegalStateException("Didn't find exactly one scenario parser (found: ${parsers.size})")
        @Suppress("UNCHECKED_CAST")
        parsers.first() as ScenarioParser<S>
    }

    private val objectMapper = ObjectMapper().registerModule(KotlinModule())
        // ignore orchestration properties
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    internal val runConfiguration by lazy {
        val configurationFile = File(System.getProperty(ConfigFileProperty))
        objectMapper.readValue<RunConfiguration>(configurationFile)
    }

    private val batchState = if (runConfiguration.duration == null) {
        RepsBasedBatchState(runConfiguration)
    } else {
        DurationBasedBatchState<RC>(runConfiguration)
    }


    private val progressReportGenerator = ProgressReportGenerator(batchState)

    override fun nextScenario() =
        batchState.nextBaseScenarios().map { generateFromBase(it) }

    override fun beforeBatch() {
        checkAllBaseScenariosCanBeParsed()
    }

    // fail early if scenarios are unparseable
    private fun checkAllBaseScenariosCanBeParsed() {
        batchState.baseScenarios.forEach {
            try {
                scenarioParser.parse(it.readText(), "")
            } catch (e: ScriptException) {
                throw IllegalStateException("Scenario in file ${it.absolutePath} is invalid", e)
            }
        }
    }

    private fun generateFromBase(scenarioFile: File): GuidedScenario {
        return GuidedScenario(
            name = "${scenarioFile.nameWithoutExtension}_${arbitraryUid()}",
            script = scenarioFile.readText(),
            baseFile = scenarioFile,
        )
    }

    private fun arbitraryUid(): String {
        val timePart = (System.currentTimeMillis() / 1000) % 100000 // gives some ordering
        val uuidPart = UUID.randomUUID().toString().take(6)
        return "%05d_%s".format(timePart, uuidPart)
    }


    override fun complete(completed: CompletedScenario<GuidedScenario, RC>, runDir: File) {
        batchState.complete(completed)
    }

    override fun createProgressReport(runDir: File, fileName: String) =
        progressReportGenerator.generate(runDir, fileName)

    override fun pcComplete() = batchState.pcComplete()

    // No dependencies in guided batches
    override fun getScenarioStatus(scenario: GuidedScenario) = ScenarioStatus.Ready


}

