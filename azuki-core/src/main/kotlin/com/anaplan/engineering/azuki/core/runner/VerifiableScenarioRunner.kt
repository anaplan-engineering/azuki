package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenarioCursor
import com.anaplan.engineering.azuki.core.system.*
import org.slf4j.LoggerFactory
import java.io.File

class VerifiableScenarioRunner<S : VerifiableScenario<AF, CF>, AF : ActionFactory, CF : CheckFactory>(
    private val implementationInstance: ImplementationInstance<AF, CF, *, *>,
    private val persistenceVerificationInstance: ImplementationInstance<AF, CF, *, *>?,
    private val scenario: S,
    private val runName: String,
) {

    fun run(): Result {
        if (scenario.iterations().isEmpty()) {
            Log.error("Verifiable scenario does not have when or then or successors!")
            RunScenarioResult(Result.NotVerifiable)
        }
        val taskResult = implementationInstance.runTask(TaskType.Verify, scenario) { implementation ->
            Log.info("Using implementation '${implementation.name}'")
            runScenario(implementation.createSystemFactory())
        }
        return if (taskResult.result?.result == Result.Verified && persistenceVerificationInstance != null) {
            val pvTaskResult =
                persistenceVerificationInstance.runTask(TaskType.PersistenceVerify, scenario) { implementation ->
                    Log.info("Using persistence verification implementation '${implementation.name}'")
                    val systemFactory = implementation.createSystemFactory() as? PersistableSystemFactory<AF, CF, *, *, *, PersistableSystem<AF, CF>>
                    if (systemFactory == null) {
                        Log.warn("Persistence verification instance specified, but system factory does not create persistable systems")
                        Result.NotPersistable
                    } else {
                        verifyPersistence(systemFactory, taskResult.result.persistenceContext!!)
                    }
                }
            pvTaskResult.result ?: Result.UnknownError
        } else {
            taskResult.result?.result ?: Result.UnknownError
        }
    }

    private data class RunScenarioResult(
        val result: Result,
        val persistenceContext: File? = null
    )

    private fun VerificationResult.toResult() =
        when (this) {
            is VerificationResult.Verified, is VerificationResult.VerifiedAndSerialized -> Result.Verified
            is VerificationResult.Unverified -> Result.Unverified
            is VerificationResult.SystemInvalid -> throw cause
        }

    private fun VerifiableScenarioCursor.State.toResult() =
        when (this) {
            VerifiableScenarioCursor.State.NoChecks -> Result.NoSupportedChecks
            VerifiableScenarioCursor.State.UnsupportedCommand -> Result.UnsupportedCommand
            VerifiableScenarioCursor.State.UnsupportedDeclaration -> Result.UnsupportedDeclaration
            else -> Result.UnknownError
        }

    private fun runScenario(systemFactory: SystemFactory<AF, CF, *, *, *, *>) =
        when (systemFactory) {
            is PersistableSystemFactory -> if (persistenceVerificationInstance == null) {
                verifyScenario(systemFactory)
            } else {
                verifyScenarioWithPeristence(systemFactory)
            }

            is ReportGeneratingSystemFactory -> generateReportFromScenario(systemFactory)
            is VerifiableSystemFactory -> verifyScenario(systemFactory)
            else -> {
                Log.error("System factory $systemFactory has no capability to handle verifiable scenario")
                RunScenarioResult(Result.IncompatibleSystem)
            }
        }

    private fun <S : ReportGeneratingSystem<AF, CF>> generateReportFromScenario(systemFactory: ReportGeneratingSystemFactory<AF, CF, *, *, *, S>): RunScenarioResult {
        val cursor = VerifiableScenarioCursor(systemFactory, scenario)
        return try {
            while (cursor.hasNext()) {
                val system = cursor.next()
                if (cursor.isEmpty) {
                    system.generateReport(runName)
                }
            }
            if (cursor.state != VerifiableScenarioCursor.State.Ok) {
                RunScenarioResult(cursor.state.toResult())
            } else {
                RunScenarioResult(Result.Reported)
            }
        } catch (e: LateDetectUnsupportedActionException) {
            RunScenarioResult(Result.UnsupportedCommand)
        } catch (e: LateDetectUnsupportedCheckException) {
            RunScenarioResult(Result.UnsupportedCheck)
        } finally {
            cursor.destroy()
        }
    }

    private fun <S : PersistableSystem<AF, CF>> verifyScenarioWithPeristence(systemFactory: PersistableSystemFactory<AF, CF, *, *, *, S>): RunScenarioResult {
        val cursor = VerifiableScenarioCursor(systemFactory, scenario)
        return try {
            val results = mutableListOf<VerificationResult>()
            while (cursor.hasNext() && results.all { it is VerificationResult.Verified }) {
                val system = cursor.next()
                results.add(
                    if (cursor.isEmpty) {
                        system.verifyAndSerialize()
                    } else {
                        system.verify()
                    }
                )
            }
            if (cursor.state != VerifiableScenarioCursor.State.Ok) {
                RunScenarioResult(cursor.state.toResult())
            } else {
                val result = results.last()
                if (result is VerificationResult.VerifiedAndSerialized) {
                    RunScenarioResult(result.toResult(), result.file)
                } else {
                    RunScenarioResult(result.toResult())
                }
            }
        } catch (e: LateDetectUnsupportedActionException) {
            RunScenarioResult(Result.UnsupportedCommand)
        } catch (e: LateDetectUnsupportedCheckException) {
            RunScenarioResult(Result.UnsupportedCheck)
        } finally {
            cursor.destroy()
        }
    }

    private fun <S : VerifiableSystem<AF, CF>> verifyScenario(systemFactory: VerifiableSystemFactory<AF, CF, *, *, *, S>): RunScenarioResult {
        val cursor = VerifiableScenarioCursor(systemFactory, scenario)
        return try {
            val results = mutableListOf<VerificationResult>()
            while (cursor.hasNext() && results.all { it is VerificationResult.Verified }) {
                val system = cursor.next()
                results.add(system.verify())
            }
            if (cursor.state != VerifiableScenarioCursor.State.Ok) {
                RunScenarioResult(cursor.state.toResult())
            } else {
                RunScenarioResult(results.last().toResult())
            }
        } catch (e: LateDetectUnsupportedActionException) {
            RunScenarioResult(Result.UnsupportedCommand)
        } catch (e: LateDetectUnsupportedCheckException) {
            RunScenarioResult(Result.UnsupportedCheck)
        } finally {
            cursor.destroy()
        }
    }


    private fun <S : PersistableSystem<AF, CF>> verifyPersistence(systemFactory: PersistableSystemFactory<AF, CF, *, *, *, S>, persistenceContext: File): Result {
        val checks = scenario.iterations().last().checks(systemFactory.checkFactory)
        if (checks.all { it is UnsupportedCheck }) {
            return Result.NoSupportedChecks
        }
        val supportedChecks = checks.filter { it !is UnsupportedCheck }
        val regardlessOfActions = scenario.regardlessOfActions(systemFactory.actionFactory)
        val system = systemFactory.create(
            SystemDefinition(
                declarations = emptyList(),
                checks = supportedChecks,
                regardlessOfActions = regardlessOfActions
            )
        )
        return try {
            system.deserializeAndVerify(persistenceContext).toResult()
        } catch (e: LateDetectUnsupportedActionException) {
            Result.UnsupportedCommand
        } catch (e: LateDetectUnsupportedCheckException) {
            Result.UnsupportedCheck
        }
    }

    companion object {
        private val Log = LoggerFactory.getLogger(VerifiableScenarioRunner::class.java)
    }

    enum class Result {
        Verified,
        Unverified,
        Reported,
        UnsupportedDeclaration,
        UnsupportedCommand,
        UnsupportedCheck,
        NoSupportedChecks,
        IncompatibleSystem,
        UnknownError,
        NotPersistable,
        NotVerifiable,
    }

}
