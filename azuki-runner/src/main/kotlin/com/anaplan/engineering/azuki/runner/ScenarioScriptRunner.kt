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
    AGF : ActionGeneratorFactory
    >(
    private val testImplementationInstance: String,
    private val oracleImplementationInstances: List<String>,
    private val scenarioImports: String,
    private val resultProcessor: ResultProcessor<AF, CF, QF, AGF> = ResultProcessor.Default()
) {

    interface ResultProcessor<
        AF : ActionFactory,
        CF : CheckFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory
        > {

        fun processOracleScenario(result: MultiOracleScenarioRunner.Result<AF, CF, QF, AGF>) {
            Log.info("Scenario completed with result: $result")
        }

        fun processVerifiableScenario(result: VerifiableScenarioRunner.Result) {
            Log.info("Scenario completed with result: $result")
            when (result) {
                VerifiableScenarioRunner.Result.Unverified -> exit("Scenario was unverified",
                    ExitCode.VerificationFailed)
                VerifiableScenarioRunner.Result.UnsupportedAction,
                VerifiableScenarioRunner.Result.UnsupportedDeclaration,
                VerifiableScenarioRunner.Result.UnsupportedCheck,
                VerifiableScenarioRunner.Result.NoSupportedChecks -> exit("Invalid scenario", ExitCode.InvalidScenario)
                VerifiableScenarioRunner.Result.IncompatibleSystem -> exit("System does not support verify/report",
                    ExitCode.InvalidSystem)
                VerifiableScenarioRunner.Result.UnknownError -> exit("There was an unexpected error",
                    ExitCode.UnknownError)
                VerifiableScenarioRunner.Result.Verified,
                VerifiableScenarioRunner.Result.Reported -> Log.info("Run completed successfully")
            }
        }

        class Default<
            AF : ActionFactory,
            CF : CheckFactory,
            QF : QueryFactory,
            AGF : ActionGeneratorFactory
            > : ResultProcessor<AF, CF, QF, AGF>
    }

    fun runScenario(scenarioScript: String) {
        val scenario = try {
            ScenarioParser.parse<BuildableScenario<AF>>(scenarioScript, scenarioImports)
        } catch (e: ScriptException) {
            exit("Scenario is invalid:\n${e.message}", ExitCode.InvalidScenario)
        }
        @Suppress("UNCHECKED_CAST")
        when (scenario) {
            is OracleScenario<*, *, *> -> runOracleScenario(scenario as OracleScenario<AF, QF, AGF>)
            is VerifiableScenario<*, *> -> runVerifiableScenario(scenario as VerifiableScenario<AF, CF>)
            else -> exit("Currently unsupported scenario type", ExitCode.UnsupportedScenarioType)
        }
    }

    private fun runOracleScenario(scenario: OracleScenario<AF, QF, AGF>) {
        val testInstance = getImplementationInstance(testImplementationInstance)
        val oracleInstances = oracleImplementationInstances.map { getImplementationInstance(it) }
        val result = MultiOracleScenarioRunner(
            testInstance,
            oracleInstances,
            scenario,
            "RunAt${System.currentTimeMillis()}").run()
        resultProcessor.processOracleScenario(result)
    }

    private fun getImplementationInstance(instanceName: String): ImplementationInstance<AF, CF, QF, AGF> {
        val implementationInstances = ImplementationInstance.getImplementationInstances<AF, CF, QF, AGF>()
        return implementationInstances.find { it.instanceName == instanceName }
            ?: exit("No implementation named $instanceName found", ExitCode.UnknownImplementation)
    }

    private fun runVerifiableScenario(scenario: VerifiableScenario<AF, CF>) {
        val implementationInstance = getImplementationInstance(testImplementationInstance)
        val result = VerifiableScenarioRunner(
            implementationInstance,
            scenario,
            "RunAt${System.currentTimeMillis()}").run()
        resultProcessor.processVerifiableScenario(result)
    }


    companion object {
        val Log = LoggerFactory.getLogger(ScenarioScriptRunner::class.java)

        private fun exit(msg: String, exitCode: ExitCode): Nothing {
            Log.error(msg)
            exitProcess(exitCode.ordinal)
        }

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
