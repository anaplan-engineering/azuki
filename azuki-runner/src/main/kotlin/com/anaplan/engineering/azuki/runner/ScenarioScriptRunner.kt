package com.anaplan.engineering.azuki.runner

import com.anaplan.engineering.azuki.core.parser.ScenarioParser
import com.anaplan.engineering.azuki.core.runner.ImplementationInstance
import com.anaplan.engineering.azuki.core.runner.MultiOracleScenarioRunner
import com.anaplan.engineering.azuki.core.runner.VerifiableScenarioRunner
import com.anaplan.engineering.azuki.core.scenario.BuildableScenario
import com.anaplan.engineering.azuki.core.scenario.OracleScenario
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory
import org.slf4j.LoggerFactory
import java.io.File
import javax.script.ScriptException
import kotlin.reflect.KClass
import kotlin.system.exitProcess


class ScenarioScriptRunner<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    S : BuildableScenario<AF>
    >(
    private val testImplementationInstanceJar: String,
    private val oracleImplementationInstanceJars: List<String>,
    private val scenarioImports: String,
) {

    fun runScenario(scenarioScript: String) {
        val scenario = try {
            ScenarioParser.parse<S>(scenarioScript, scenarioImports)
        } catch (e: ScriptException) {
            exit("Scenario is invalid:\n${e.message}", ExitCode.InvalidScenario)
        }
        when (scenario) {
            is OracleScenario<*, *, *> -> runOracleScenario(scenario)
            is VerifiableScenario<*, *> -> runVerifiableScenario(scenario)
            else -> exit("Currently unsupported scenario type", ExitCode.UnsupportedScenarioType)
        }
    }

    private fun runOracleScenario(scenario: S) {
        val testInstance = getImplementationInstance(testImplementationInstanceJar)
        val oracleInstances = oracleImplementationInstanceJars.map { getImplementationInstance(it) }
        val result = construct(MultiOracleScenarioRunner::class,
            testInstance,
            oracleInstances,
            scenario,
            "RunAt${System.currentTimeMillis()}").run()
        Log.info("Scenario completed with result: $result")
    }

    private fun getImplementationInstance(instanceName: String): ImplementationInstance<AF, CF, QF, AGF> {
        val implementationInstances = ImplementationInstance.getImplementationInstances<AF, CF, QF, AGF>()
        return implementationInstances.find { it.instanceName == instanceName }
            ?: exit("No implementation named $testImplementationInstanceJar found", ExitCode.UnknownImplementation)
    }

    private fun runVerifiableScenario(scenario: S) {
        val implementationInstance = getImplementationInstance(testImplementationInstanceJar)
        val result = construct(VerifiableScenarioRunner::class,
            implementationInstance,
            scenario,
            "RunAt${System.currentTimeMillis()}").run()
        Log.info("Scenario completed with result: $result")
        when (result) {
            VerifiableScenarioRunner.Result.Unverified -> exit("Scenario was unverified", ExitCode.VerificationFailed)
            VerifiableScenarioRunner.Result.UnsupportedAction,
            VerifiableScenarioRunner.Result.UnsupportedDeclaration,
            VerifiableScenarioRunner.Result.UnsupportedCheck,
            VerifiableScenarioRunner.Result.NoSupportedChecks -> exit("Invalid scenario", ExitCode.InvalidScenario)
            VerifiableScenarioRunner.Result.IncompatibleSystem -> exit("System does not support verify/report",
                ExitCode.InvalidSystem)
            VerifiableScenarioRunner.Result.UnknownError -> exit("There was an unexpected error", ExitCode.UnknownError)
            VerifiableScenarioRunner.Result.Verified,
            VerifiableScenarioRunner.Result.Reported -> Log.info("Run completed successfully")
        }
    }

    fun exit(msg: String, exitCode: ExitCode): Nothing {
        Log.error(msg)
        exitProcess(exitCode.ordinal)
    }

    companion object {
        val Log = LoggerFactory.getLogger(ScenarioScriptRunner::class.java)
    }
}

// we don't have any means to infer generics from the environment
private fun <T : Any> construct(c: KClass<T>, vararg args: Any) =
    c.constructors.single().call(*args)


fun main(args: Array<String>) {
    val (scriptFile, importsFile, testImpl) = args.take(3)
    val script = File(scriptFile).readText()
    val imports = File(importsFile).readText()
    val oracleImpls = args.drop(3)
    ScenarioScriptRunner.Log.debug("Starting script runner: testImpl=$testImpl oracleImpls=$oracleImpls")
    val runner = construct(ScenarioScriptRunner::class, testImpl, oracleImpls, imports)
    runner.runScenario(script)

    // something appears to be keeping this alive -- can't figure out
    // using exit as last resort for now!
    exitProcess(ExitCode.Ok.ordinal)
}

enum class ExitCode {
    Ok,  // should not be used directly, but reserves 0-value
    InvalidScenario,
    InvalidSystem,
    VerificationFailed,
    QueryFailed,
    UnsupportedScenarioType,
    UnknownError,
    UnknownImplementation,
}
