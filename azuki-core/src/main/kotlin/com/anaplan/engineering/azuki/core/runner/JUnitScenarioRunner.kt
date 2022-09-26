package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.JvmSystemProperties.excludeImplFromEacDescriptionPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.forceKnownBugsPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.junitTimeoutPropertyName
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.junit.Assume
import org.junit.Ignore
import org.junit.internal.runners.model.ReflectiveCallable
import org.junit.rules.Timeout
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.Parameterized.Parameters
import org.junit.runners.ParentRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import org.junit.runners.model.TestClass
import org.slf4j.LoggerFactory
import java.lang.System
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Eac(
    // the summary should contain a concise definition of the AC
    val summary: String,
    // notes should contain further details including examples and impl level explanations
    vararg val notes: String
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModellingExample(
    val summary: String
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExtendedTimeout(
    val timeout: Long,
    val timeUnit: TimeUnit
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalysisScenario

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GeneratedScenario

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AdapterTest(
    val expectSkip: Boolean = false
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RestrictTo(
    val implementationName: String
)


data class ScenarioRun<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    val name: String,
    val method: FrameworkMethod,
    val implementationInstance: ImplementationInstance<AF, CF, QF, AGF>,
    val eacMetadata: EacMetadata? = null,
    val ignoreWhenUnsupported: Boolean = true,
    val expectSkip: Boolean = false,
    val parameters: Array<Any>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScenarioRun<*, *, *, *>

        if (name != other.name) return false
        if (method != other.method) return false
        if (implementationInstance != other.implementationInstance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + implementationInstance.hashCode()
        return result
    }

    override fun toString() = "$name [${implementationInstance.implementationName}]"
}

class JUnitScenarioRunner<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    S : VerifiableScenario<AF, CF>,
    >(private val testClass: Class<*>) : ParentRunner<ScenarioRun<AF, CF, QF, AGF>>(testClass) {

    companion object {
        private val defaultTimeout =
            Timeout(System.getProperty(junitTimeoutPropertyName, "3").toLong(), TimeUnit.MINUTES)

        private val Log = LoggerFactory.getLogger(JUnitScenarioRunner::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    private val kClass = testClass.kotlin as KClass<S>

    private val runKnownBugs by lazy {
        System.getProperty(forceKnownBugsPropertyName, "false").toBoolean()
    }

    override fun isIgnored(child: ScenarioRun<AF, CF, QF, AGF>): Boolean {
        if (child.method.getAnnotation(Ignore::class.java) != null) {
            return true
        }
        val knownBug = child.method.getAnnotation(KnownBug::class.java)
        val implementationName = child.implementationInstance.implementationName
        if (knownBug != null && !runKnownBugs && implementationName in knownBug.issues.map { it.implementation }) {
            Log.warn("Skipping ${child.method.declaringClass.name}.${child.method.name} as this exhibits a known bug in $implementationName")
            return true
        }
        val toBeDone = child.method.getAnnotation(ToBeDone::class.java)
        if (toBeDone != null && implementationName in toBeDone.issues.map { it.implementation }) {
            Log.warn("Skipping ${child.method.declaringClass.name}.${child.method.name} as this is still TBD in $implementationName")
            return true
        }
        val unsupported = child.method.getAnnotation(Unsupported::class.java)
        if (unsupported != null && implementationName in unsupported.implementation) {
            Log.warn("Skipping ${child.method.declaringClass.name}.${child.method.name} as unsupported in $implementationName")
            return true
        }
        val restrictTo = child.method.getAnnotation(RestrictTo::class.java)
        return restrictTo != null && restrictTo.implementationName != implementationName
    }

    override fun runChild(child: ScenarioRun<AF, CF, QF, AGF>, notifier: RunNotifier) {
        val description = describeChild(child)
        if (isIgnored(child)) {
            notifier.fireTestIgnored(description)
        } else {
            val timeout = child.method.getTimeout()
            runLeaf(timeout.apply(createRunStatement(child), description), description, notifier)
        }
    }

    private fun createRunStatement(run: ScenarioRun<AF, CF, QF, AGF>) =
        ScenarioInvoker(
            run.method.method,
            kClass,
            run.implementationInstance,
            run.eacMetadata,
            run.ignoreWhenUnsupported,
            run.expectSkip,
            run.parameters,
        )

    private class ScenarioInvoker<
        AF : ActionFactory,
        CF : CheckFactory,
        QF : QueryFactory,
        AGF : ActionGeneratorFactory,
        S : VerifiableScenario<AF, CF>
        >(
        val build: Method,
        val testClass: KClass<S>,
        val implementationInstance: ImplementationInstance<AF, CF, QF, AGF>,
        val eacMetadata: EacMetadata?,
        val ignoreWhenUnsupported: Boolean,
        val expectSkip: Boolean,
        val parameters: Array<Any>?,
    ) : Statement() {
        override fun evaluate() {
            object : ReflectiveCallable() {
                override fun runReflectiveCall(): Any {
                    try {
                        val scenario = if (parameters == null) {
                            testClass.primaryConstructor!!.call()
                        } else {
                            testClass.primaryConstructor!!.call(*parameters)
                        }
                        build.invoke(scenario)
                        val scenarioName = eacMetadata?.scenarioName ?: "${build.declaringClass.name}-${build.name}"
                        val verifiableScenarioRunner =
                            VerifiableScenarioRunner(implementationInstance, scenario, scenarioName)
                        when (verifiableScenarioRunner.run()) {
                            VerifiableScenarioRunner.Result.UnsupportedAction -> unsupported("Skipping - unsupported action found")
                            VerifiableScenarioRunner.Result.UnsupportedDeclaration -> unsupported("Skipping - unsupported declaration found")
                            VerifiableScenarioRunner.Result.UnsupportedCheck -> unsupported("Skipping - unsupported check found")
                            VerifiableScenarioRunner.Result.NoSupportedChecks -> unsupported("Skipping - no supported checks found")
                            VerifiableScenarioRunner.Result.Unverified -> throw AssertionError("Verification checks failed")
                            VerifiableScenarioRunner.Result.IncompatibleSystem -> throw SkippedException("Skipping - system does not support verify or report")
                            VerifiableScenarioRunner.Result.UnknownError -> throw IllegalStateException("Unexpected error running scenario")
                            VerifiableScenarioRunner.Result.Verified,
                            VerifiableScenarioRunner.Result.Reported -> {
                            } // success!
                        }
                        if (EacMetadataRecorder.recording && eacMetadata != null) {
                            EacMetadataRecorder.record(eacMetadata)
                        }
                    } catch (e: Throwable) {
                        if (e !is SkippedException || !expectSkip) {
                            throw e
                        }
                    }
                    return true
                }
            }.run()
        }

        private fun unsupported(msg: String) {
            // was previously checking if implementation was total as part of this.. should we move that into runner?
            if (ignoreWhenUnsupported) {
                Assume.assumeTrue(false)
            } else {
                throw SkippedException(msg)
            }
        }

    }

    class SkippedException(msg: String) : Exception(msg)

    private val parameterMethod: FrameworkMethod? by lazy {
        val companionObject = kClass.companionObjectInstance
        if (companionObject == null) {
            null
        } else {
            val parameterMethods = TestClass(companionObject.javaClass).getAnnotatedMethods(Parameters::class.java)
            Log.debug("Parameter methods: $parameterMethods")
            if (parameterMethods.size > 1) {
                throw IllegalStateException("Multiple parameter methods declared for $testClass")
            }
            if (parameterMethods.isEmpty()) null else parameterMethods.first()
        }
    }

    private fun FrameworkMethod.getTimeout(): Timeout {
        val annotation = getAnnotation(ExtendedTimeout::class.java)
        return if (annotation == null) {
            defaultTimeout
        } else {
            Timeout(annotation.timeout, annotation.timeUnit)
        }
    }


    override fun getChildren(): MutableList<ScenarioRun<AF, CF, QF, AGF>> {
        Log.debug("Getting children: $testClass")
        val implementationInstances = ImplementationInstance.getImplementationInstances<AF, CF, QF, AGF>()
        Log.debug("Available implementation instances: $implementationInstances")
        val eacs = getTestClass().getAnnotatedMethods(Eac::class.java).flatMap { method ->
            val eac = method.getAnnotation(Eac::class.java)!!
            implementationInstances.map { implementationInstance ->
                val beh = getTestClass().getAnnotation(BEH::class.java)
                val eacMetadata = if (beh == null) {
                    null
                } else {
                    EacMetadata(
                        functionalElement = beh.functionalElement,
                        behavior = beh.behavior,
                        behaviorSummary = beh.summary.trim(),
                        methodName = method.name,
                        acceptanceCriteria = eac.summary.trim(),
                        implementation = implementationInstance.implementationName,
                    )
                }
                ScenarioRun(eac.summary,
                    method,
                    implementationInstance,
                    eacMetadata = eacMetadata)
            }
        }
        val modellingExamples = getTestClass().getAnnotatedMethods(ModellingExample::class.java).flatMap { method ->
            val modellingExample = method.getAnnotation(ModellingExample::class.java)!!
            implementationInstances.map { implementationInstance ->
                ScenarioRun(modellingExample.summary,
                    method,
                    implementationInstance)
            }
        }
        val adapterTests = getTestClass().getAnnotatedMethods(AdapterTest::class.java).flatMap { method ->
            val adapterTest = method.getAnnotation(AdapterTest::class.java)!!
            implementationInstances.map { implementationInstance ->
                ScenarioRun(method.name,
                    method,
                    implementationInstance,
                    ignoreWhenUnsupported = false,
                    expectSkip = adapterTest.expectSkip)
            }
        }
        val analysisScenarios = getTestClass().getAnnotatedMethods(AnalysisScenario::class.java).flatMap { method ->
            implementationInstances.map { implementationInstance ->
                ScenarioRun(method.name, method, implementationInstance)
            }
        }
        val generatedScenarios = getTestClass().getAnnotatedMethods(GeneratedScenario::class.java).flatMap { method ->
            implementationInstances.map { implementationInstance ->
                ScenarioRun(method.name, method, implementationInstance)
            }
        }
        val nonParameterizedRuns = eacs + adapterTests + analysisScenarios + generatedScenarios + modellingExamples
        return (if (parameterMethod == null) nonParameterizedRuns else parameterize(nonParameterizedRuns)).toMutableList()
    }

    private fun parameterize(baseRuns: List<ScenarioRun<AF, CF, QF, AGF>>): List<ScenarioRun<AF, CF, QF, AGF>> {
        val parameterPermutations =
            parameterMethod!!.method.invoke(kClass.companionObjectInstance!!) as? Collection<Array<Any>>
                ?: throw IllegalStateException("Parameter method $parameterMethod. returns object with invalid type")
        Log.debug("Test is parameterized, parameter method: ${parameterMethod?.name}, permutation count: ${parameterPermutations.size}")
        return baseRuns.flatMap { baseRun -> parameterPermutations.map { perm -> baseRun.copy(parameters = perm) } }
    }

    override fun describeChild(child: ScenarioRun<AF, CF, QF, AGF>): Description {
        val parameterSuffix = if (child.parameters == null) "" else "${child.parameters.toList()}"
        val methodName = "${child.method.name}$parameterSuffix"
        return if (excludeImplFromDescription) {
            Description.createTestDescription(testClass.name, methodName)
        } else {
            Description.createTestDescription("${child.implementationInstance.implementationName}-${testClass.name}",
                methodName)
        }
    }

    private val excludeImplFromDescription by lazy {
        System.getProperty(excludeImplFromEacDescriptionPropertyName, "false").toBoolean()
    }

}
