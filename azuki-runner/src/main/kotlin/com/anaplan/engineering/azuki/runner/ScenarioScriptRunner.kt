package com.anaplan.engineering.azuki.runner

import com.anaplan.engineering.azuki.core.parser.SimpleScenarioParser
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
import org.slf4j.Logger
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

        // TODO - have process functions return exit code and handle exit downstream
        fun processOracleScenario(result: MultiOracleScenarioRunner.Result<AF, CF, QF, AGF>) {
            Log.info("Scenario completed with result: $result")
        }

        fun processVerifiableScenario(result: VerifiableScenarioRunner.Result) {
            Log.info("Scenario completed with result: $result")
            when (result) {
                VerifiableScenarioRunner.Result.Unverified -> exit("Scenario was unverified",
                    ExitCode.VerificationFailed)
                VerifiableScenarioRunner.Result.UnsupportedCommand,
                VerifiableScenarioRunner.Result.UnsupportedDeclaration,
                VerifiableScenarioRunner.Result.UnsupportedCheck,
                VerifiableScenarioRunner.Result.NoSupportedChecks,
                VerifiableScenarioRunner.Result.NotVerifiable -> exit("Invalid scenario", ExitCode.InvalidScenario)
                VerifiableScenarioRunner.Result.IncompatibleSystem -> exit("System does not support verify/report",
                    ExitCode.InvalidSystem)
                VerifiableScenarioRunner.Result.UnknownError -> exit("There was an unexpected error",
                    ExitCode.UnknownError)
                VerifiableScenarioRunner.Result.NotPersistable -> exit("There was a configuration issue: a persistable system was expected",
                    ExitCode.NotPersistable)
                VerifiableScenarioRunner.Result.Verified,
                VerifiableScenarioRunner.Result.Reported -> Log.info("Run completed successfully")
            }
        }

        fun handleError(error: Throwable) {
            when (error) {
                is InvalidScenarioException -> exit("Scenario is invalid:\n${error.cause?.message}",
                    ExitCode.InvalidScenario)

                is UnsupportedScenarioTypeException -> exit("Currently unsupported scenario type: ${error.type}",
                    ExitCode.UnsupportedScenarioType)

                else -> exit("Unknown error", ExitCode.UnknownError)
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
        try {
            val scenario = try {
                SimpleScenarioParser<BuildableScenario<AF>>().parse(scenarioScript, scenarioImports)
            } catch (e: ScriptException) {
                resultProcessor.handleError(InvalidScenarioException(e))
                return
            }
            @Suppress("UNCHECKED_CAST")
            when (scenario) {
                is OracleScenario<*, *, *> -> runOracleScenario(scenario as OracleScenario<AF, QF, AGF>)
                is VerifiableScenario<*, *> -> runVerifiableScenario(scenario as VerifiableScenario<AF, CF>)
                else -> resultProcessor.handleError(UnsupportedScenarioTypeException(scenario::class))
            }
        } catch (t: Throwable) {
            resultProcessor.handleError(t)
            throw t
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
            getPersistenceVerificationInstance(),
            scenario,
            "RunAt${System.currentTimeMillis()}").run()
        resultProcessor.processVerifiableScenario(result)
    }

    private fun getPersistenceVerificationInstance() =
        if (ImplementationInstance.havePersistenceVerificationInstance) {
            ImplementationInstance.getPersistenceVerificationInstance<AF, CF, QF, AGF>()
        } else {
            null
        }


    companion object {
        val Log: Logger = LoggerFactory.getLogger(ScenarioScriptRunner::class.java)

        fun exit(msg: String, exitCode: ExitCode): Nothing {
            Log.error(msg)
            exitProcess(exitCode.ordinal)
        }
    }

    class InvalidScenarioException(e: ScriptException) : RuntimeException(e)
    class UnsupportedScenarioTypeException(val type: KClass<*>) : RuntimeException()
}

// we don't have any means to infer generics from the environment
private fun <T : Any> construct(c: KClass<T>, vararg args: Any) =
    c.constructors.single().call(*args)


fun main(args: Array<String>) {
    val (scriptFile, importsFile, testImpl) = args.take(3)
    val script = File(scriptFile).readText()
    val imports = File(importsFile).readText()
    val oracleImpls = args.drop(3)
    ScenarioScriptRunner.Log.debug("Starting script runner: testImpl={} oracleImpls={}", testImpl, oracleImpls)
    val defaultResultProcessor = construct(ScenarioScriptRunner.ResultProcessor.Default::class)
    val runner = construct(ScenarioScriptRunner::class, testImpl, oracleImpls, imports, defaultResultProcessor)
    runner.runScenario(script)

    // something appears to be keeping this alive -- can't figure out
    // using exit as last resort for now!
    exitProcess(ExitCode.Ok.ordinal)
}


enum class ExitCode(val category: Category) {
    Ok(Category.Ok),
    InvalidScenario(Category.Incomplete),
    InvalidSystem(Category.Incomplete),
    FailedDependency(Category.Incomplete),
    TimeOut(Category.Incomplete),
    VerificationFailed(Category.Unverified),
    QueryFailed(Category.Error),
    UnsupportedScenarioType(Category.Error),
    UnknownError(Category.Error),
    UnknownImplementation(Category.Error),
    NotPersistable(Category.Error);

    enum class Category {
        Ok,
        Incomplete,
        Unverified,
        Error
    }

}
