package com.anaplan.engineering.azuki.core.runner

import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface AzukiRunListener {
    fun scenarioComplete(scenarioResult: ScenarioResult)
    fun suiteComplete(suiteName: String) {}
    fun runComplete() {}
}

internal object AzukiJUnitRunListener : RunListener() {

    private val runState = ConcurrentHashMap<String, ScenarioRunState>()

    private data class ScenarioRunState(
        val description: Description,
        val start: Long = System.nanoTime(),
        var state: JUnitScenarioResult = JUnitScenarioResult.Verified
    ) {
        fun toResult() =
            ScenarioResult(
                className = description.className,
                scenarioName = description.methodName,
                implementationName = testImplementation.name,
                implementationVersion = testImplementation.version.ifBlank { null },
                persistenceImplementationName = persistenceImplementation?.name,
                persistenceImplementationVersion = persistenceImplementation?.version,
                state = state,
                durationNs = System.nanoTime() - start,
                scenarioType = scenarioInfo.type,
                issues = scenarioInfo.issues.toSet(),
            )

        val testImplementation by lazy { description.annotations.filterIsInstance<TestImplementation>().single() }

        val scenarioInfo by lazy { description.annotations.filterIsInstance<ScenarioInfo>().single() }

        val persistenceImplementation by lazy {
            description.annotations.filterIsInstance<PersistenceImplementation>().singleOrNull()
        }
    }

    private fun Description.uniqueId(): String {
        val testImplementation = annotations.filterIsInstance<TestImplementation>().single()
        val persistenceImplementation = annotations.filterIsInstance<PersistenceImplementation>().singleOrNull()
        return listOfNotNull(
            className,
            methodName,
            testImplementation.name,
            testImplementation.version.ifBlank { null },
            persistenceImplementation?.name,
            persistenceImplementation?.version,
        ).joinToString(":")
    }

    override fun testFinished(description: Description) {
        val state = runState.remove(description.uniqueId())
        if (state != null) {
            listeners.forEach { it.scenarioComplete(state.toResult()) }
        }
    }

    override fun testFailure(failure: Failure) {
        runState[failure.description.uniqueId()]?.state =
            when (val e = failure.exception) {
                is JUnitScenarioRunner.ScenarioResultHolder -> e.result
                else -> JUnitScenarioResult.Unverified
            }
    }

    override fun testIgnored(description: Description) {
        // If annotated, case will be ignored before it is started
        if (!runState.containsKey(description.uniqueId())) {
            testStarted(description)
        }
        val scenarioResult = runState[description.uniqueId()]!!
        scenarioResult.state = if (scenarioResult.scenarioInfo.knownBug) {
            JUnitScenarioResult.KnownBug
        } else if (scenarioResult.scenarioInfo.toBeDone) {
            JUnitScenarioResult.ToBeDone
        } else {
            JUnitScenarioResult.Unsupported
        }
        testFinished(description)
    }

    override fun testSuiteFinished(description: Description) {
        listeners.forEach { it.suiteComplete(description.displayName) }
    }


    // N.B. This is never called by Gradle: https://github.com/gradle/gradle/issues/842
    override fun testRunFinished(result: Result) {
        listeners.forEach { it.runComplete() }
    }

    override fun testAssumptionFailure(failure: Failure) {
        runState[failure.description.uniqueId()]?.state =
            when (val e = failure.exception) {
                is JUnitScenarioRunner.ScenarioResultHolder -> e.result
                else -> JUnitScenarioResult.Unsupported
            }
    }


    override fun testStarted(description: Description) {
        runState[description.uniqueId()] = ScenarioRunState(description)
    }

    private val listeners by lazy {
        val loader = ServiceLoader.load(AzukiRunListener::class.java)
        loader.iterator().asSequence().filterIsInstance<AzukiRunListener>().toList()
    }

    val hasListeners by lazy { listeners.isNotEmpty() }

}

data class ScenarioResult(
    val className: String,
    val scenarioName: String,
    val scenarioType: JUnitScenarioType,
    val implementationName: String,
    val implementationVersion: String?,
    val persistenceImplementationName: String?,
    val persistenceImplementationVersion: String?,
    val state: JUnitScenarioResult,
    val issues: Set<String>,
    val durationNs: Long
)


