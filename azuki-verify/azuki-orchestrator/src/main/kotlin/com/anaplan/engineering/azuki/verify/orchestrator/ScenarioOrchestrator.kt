package com.anaplan.engineering.azuki.verify.orchestrator

import com.anaplan.engineering.azuki.runner.ExitCode
import com.anaplan.engineering.azuki.verify.batch.api.*
import com.anaplan.engineering.azuki.verify.orchestrator.configuration.RunConfiguration
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess


@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ScenarioOrchestrator<OS : OrchestratableScenario, RC : ScenarioResultContext>(
    testImplementationInstance: String,
    oracleImplementationInstances: List<String>,
    private val resultProcessor: ResultProcessor<RC>
) {

    private val runConfiguration by lazy {
        val configurationFile = File(System.getProperty(ConfigFileProperty))
        objectMapper.readValue<RunConfiguration>(configurationFile)
    }

    private val verificationEnvironment by lazy {
        VerificationEnvironment(
            testImplementationInstance = testImplementationInstance,
            oracleImplementationInstances = oracleImplementationInstances,
            importFile = File(runConfiguration.scenarios.importFile),
            killAgentBinary = if (runConfiguration.admin.killAgentPath == null) null else File(runConfiguration.admin.killAgentPath!!),
            forkConfiguration = runConfiguration.admin.fork,
            testPackage = runConfiguration.testPackage,
            verifiedTestsDir = runConfiguration.verifiedTestsDir,
            unverifiedTestsDir = runConfiguration.unverifiedTestsDir,
            junitResultsDir = File(runConfiguration.runDir, "junit-reports"),
        )
    }

    private val forkedJvmRunner by lazy {
        ForkedJvmRunner(verificationEnvironment)
    }

    private val scenarioBatch: OrchestratableScenarioBatch<OS, RC> by lazy {
        val loader = ServiceLoader.load(OrchestratableScenarioBatch::class.java)
        val batches = loader.iterator().asSequence().toList()
        if (batches.size != 1) throw IllegalStateException("Didn't find exactly one scenario batch (found: ${batches.size})")
        @Suppress("UNCHECKED_CAST")
        batches.first() as OrchestratableScenarioBatch<OS, RC>
    }

    private suspend fun runGenerator(until: () -> Boolean, channel: Channel<OS>) {
        while (until()) {
            val scenarios = scenarioBatch.nextScenario()
            scenarios.forEach { orchestratableScenario ->
                Log.info("scenario=${orchestratableScenario.name}, action=generated")
                channel.send(orchestratableScenario)
            }
        }
    }

    fun processBatch() {
        Log.info("Start processing scenario batch testImplementationInstance=${verificationEnvironment.testImplementationInstance} oracleImplementationInstances=${verificationEnvironment.oracleImplementationInstances}")
        val completedScenarioManager = CompletedScenarioManager(runConfiguration, scenarioBatch)
        val scenarioDependencyManager = ScenarioDependencyManager(scenarioBatch)

        runBlocking {
            val generatedChannel = Channel<OS>()
            val readyChannel = Channel<OS>()
            val completedChannel = Channel<CompletedScenario<OS, RC>>(Channel.UNLIMITED)

            launch(CoroutineName("Generator")) {
                // generator will mostly block the main thread until complete so other coroutines should use
                runGenerator({ scenarioBatch.pcComplete() < 100 }, generatedChannel)
                Log.info("All required scenarios generated -- closing generated channel")
                generatedChannel.close()
            }

            coroutineScope {
                launch(newSingleThreadContext("DependencyManager")) {
                    scenarioDependencyManager.manageScenarioDependencies(
                        generatedChannel,
                        readyChannel,
                        completedChannel
                    )
                }

                val runners = (1..runConfiguration.admin.maxParallelRuns).map {
                    launch(newSingleThreadContext("ScenarioRunner-$it")) {
                        runScenario(readyChannel, completedChannel)
                    }
                }

                launch(CoroutineName("RunnerManager")) {
                    runners.forEach { it.join() }
                    Log.info("All required scenarios run -- closing completed channel")
                    completedChannel.close()
                }

                if (runConfiguration.admin.report.generateEvery > 0) {
                    launch(newSingleThreadContext("SummaryReporter")) {
                        while (!completedChannel.isClosedForReceive) {
                            delay(1000 * runConfiguration.admin.report.generateEvery)
                            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            val reportName = "${runConfiguration.admin.report.prefix}-${timestamp}"
                            scenarioBatch.createProgressReport(runConfiguration.runDir, "$reportName.html")
                            Log.info("Batch ${scenarioBatch.pcComplete()}% complete")
                        }
                    }
                }

                launch(newSingleThreadContext("CompletionManager")) {
                    completedScenarioManager.completeScenarios(completedChannel)
                }
            }
        }

        // TODO - parallelize
        scenarioBatch.createProgressReport(runConfiguration.runDir, "${runConfiguration.admin.report.prefix}.html")
    }

    private fun writeScenarioText(orchestratableScenario: OrchestratableScenario): File {
        val runDir = orchestratableScenario.getResultDirectory(runConfiguration.runDir)
        runDir.mkdirs()
        val scenarioFile = runDir.resolve("${orchestratableScenario.name}.scn")
        scenarioFile.writeText(orchestratableScenario.script)
        return scenarioFile
    }

    private suspend fun runScenario(
        readyChannel: Channel<OS>,
        completedChannel: Channel<CompletedScenario<OS, RC>>
    ) {
        readyChannel.consumeEach { orchestratableScenario ->
            val completedScenario = try {
                Log.info("scenario=${orchestratableScenario.name}, action=run")
                val scenarioFile = writeScenarioText(orchestratableScenario)
                val processResult = forkedJvmRunner.forkAndRun(
                    orchestratableScenario.name,
                    scenarioFile,
                    verificationEnvironment.importFile,
                    verificationEnvironment.oracleImplementationInstances,
                    verificationEnvironment.testImplementationInstance,
                    if (orchestratableScenario is TestScenario) orchestratableScenario.packageName else null,
                    if (orchestratableScenario is TestScenario) orchestratableScenario.className else null,
                )
                CompletedScenario(
                    orchestratableScenario,
                    processResult.exitCode,
                    resultProcessor.processResult(scenarioFile.parentFile),
                    error = processResult.exception
                )
            } catch (e: Exception) {
                Log.error("Unexpected exception when running scenario", e)
                CompletedScenario(
                    orchestratableScenario,
                    ExitCode.UnknownError,
                    error = e)
            } catch (e: Error) {
                Log.error("Fatal error when running scenario", e)
                // Is there a neater way to do this?
                exitProcess(999)
            }
            Log.info("scenario=${orchestratableScenario.name}, action=markCompleted")
            completedChannel.send(completedScenario)
        }
    }

    companion object {
        val Log = LoggerFactory.getLogger(ScenarioOrchestrator::class.java)
    }

    interface ResultProcessor<RC : ScenarioResultContext> {
        fun processResult(scenarioDir: File): RC
    }
}

internal val objectMapper = ObjectMapper().registerModule(KotlinModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun main(args: Array<String>) {
    if (args.size < 3) {
        System.err.println("Expect name of configuration file, test instance and at least one oracle instance")
    }
    val (configFile, testImplementationInstance) = args.take(2)
    val oracleImplementationInstances = args.drop(2)
    System.setProperty(ConfigFileProperty, configFile)
    ScenarioOrchestrator<OrchestratableScenario, DummyResultContext>(
        testImplementationInstance,
        oracleImplementationInstances,
        DummyResultProcessor
    ).processBatch()
}

private object DummyResultContext : ScenarioResultContext {
    override val reportFields = emptyMap<String, String>()
}

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
private object DummyResultProcessor : ScenarioOrchestrator.ResultProcessor<DummyResultContext> {
    override fun processResult(scenarioDir: File) = DummyResultContext
}

