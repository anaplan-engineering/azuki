package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.system.EacMetadata
import com.anaplan.engineering.azuki.core.system.EacMetadataRecorder
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.core.system.Implementation
import org.junit.Ignore
import org.junit.internal.runners.model.ReflectiveCallable
import org.junit.internal.runners.statements.Fail
import org.junit.rules.Timeout
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

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


data class ScenarioRun(
    val name: String,
    val method: FrameworkMethod,
    val implementation: Implementation<*, *, *, *, *>,
    val eacMetadata: EacMetadata? = null,
    val ignoreWhenUnsupported: Boolean = true,
    val expectSkip: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScenarioRun

        if (name != other.name) return false
        if (method != other.method) return false
        if (implementation != other.implementation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + implementation.hashCode()
        return result
    }

    override fun toString() = "$name [${implementation.name}]"
}

class JUnitScenarioRunner(private val testClass: Class<*>) : ParentRunner<ScenarioRun>(testClass) {

    companion object {
        private val defaultTimeout = Timeout(3, TimeUnit.MINUTES)
    }

    private val runKnownBugs by lazy {
        System.getProperty("run.known.bugs")?.toBoolean() ?: false
    }

    private val runMethod by lazy {
        getTestClass().javaClass.getMethod("run", String::class.java, Boolean::class.java)
            ?: throw IllegalStateException("Must implement `run` method")
    }

    override fun isIgnored(child: ScenarioRun): Boolean {
        if (child.method.getAnnotation(Ignore::class.java) != null) {
            return true
        }
        val knownBug = child.method.getAnnotation(KnownBug::class.java)
        if (knownBug != null && !runKnownBugs && child.implementation.name in knownBug.issues.map { it.implementation }) {
            System.err.println("Skipping ${child.method.declaringClass.name}.${child.method.name} as this exhibits a known bug in ${child.implementation.name}")
            return true
        }
        val toBeDone = child.method.getAnnotation(ToBeDone::class.java)
        if (toBeDone != null && child.implementation.name in toBeDone.issues.map { it.implementation }) {
            System.err.println("Skipping ${child.method.declaringClass.name}.${child.method.name} as this is still TBD in ${child.implementation.name}")
            return true
        }
        val unsupported = child.method.getAnnotation(Unsupported::class.java)
        if (unsupported != null && child.implementation.name in unsupported.implementation) {
            System.err.println("Skipping ${child.method.declaringClass.name}.${child.method.name} as unsupported in ${child.implementation.name}")
            return true
        }
        val restrictTo = child.method.getAnnotation(RestrictTo::class.java)
        return restrictTo != null && restrictTo.implementationName != child.implementation.name
    }

    override fun runChild(child: ScenarioRun, notifier: RunNotifier) {
        val description = describeChild(child)
        if (isIgnored(child)) {
            notifier.fireTestIgnored(description)
        } else {
            println("Running '$child'")
            val timeout = child.method.getTimeout()
            runLeaf(timeout.apply(createRunStatement(child), description), description, notifier)
        }
    }

    private fun createRunStatement(run: ScenarioRun): Statement {
        val testObject = try {
            object : ReflectiveCallable() {
                override fun runReflectiveCall(): Any {
                    return testClass.constructors.first().newInstance(run.implementation)
                }
            }.run()
        } catch (e: Throwable) {
            return Fail(e)
        }
        return ScenarioInvoker(run.method.method,
            runMethod,
            testObject,
            run.eacMetadata,
            run.ignoreWhenUnsupported,
            run.expectSkip)
    }

    private class ScenarioInvoker(
        val build: Method,
        val run: Method,
        val testObject: Any,
        val eacMetadata: EacMetadata?,
        val ignoreWhenUnsupported: Boolean,
        val expectSkip: Boolean,
    ) : Statement() {
        override fun evaluate() {
            object : ReflectiveCallable() {
                override fun runReflectiveCall(): Any {
                    build.invoke(testObject)
                    try {
                        val scenarioName = eacMetadata?.scenarioName ?: "${build.declaringClass.name}-${build.name}"
                        run.invoke(testObject, scenarioName, ignoreWhenUnsupported)
                        if (EacMetadataRecorder.recording && eacMetadata != null) {
                            EacMetadataRecorder.record(eacMetadata)
                        }
                    } catch (e: Throwable) {
                        if (e.cause !is SkippedException || !expectSkip) {
                            throw e
                        }
                    }
                    return true
                }
            }.run()
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

    override fun getChildren(): MutableList<ScenarioRun> {
        val eacs = getTestClass().getAnnotatedMethods(Eac::class.java).flatMap { method ->
            val eac = method.getAnnotation(Eac::class.java)!!
            Implementation.implementations.map { implementation ->
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
                        implementation = implementation.name,
                    )
                }
                ScenarioRun(eac.summary, method, implementation, eacMetadata = eacMetadata)
            }
        }
        val modellingExamples = getTestClass().getAnnotatedMethods(ModellingExample::class.java).flatMap { method ->
            val modellingExample = method.getAnnotation(ModellingExample::class.java)!!
            Implementation.implementations.map { implementation ->
                ScenarioRun(modellingExample.summary, method, implementation)
            }
        }
        val adapterTests = getTestClass().getAnnotatedMethods(AdapterTest::class.java).flatMap { method ->
            val adapterTest = method.getAnnotation(AdapterTest::class.java)!!
            Implementation.implementations.map { implementation ->
                ScenarioRun(method.name,
                    method,
                    implementation,
                    ignoreWhenUnsupported = false,
                    expectSkip = adapterTest.expectSkip)
            }
        }
        val analysisScenarios = getTestClass().getAnnotatedMethods(AnalysisScenario::class.java).flatMap { method ->
            Implementation.implementations.map { implementation ->
                ScenarioRun(method.name, method, implementation)
            }
        }
        val generatedScenarios = getTestClass().getAnnotatedMethods(GeneratedScenario::class.java).flatMap { method ->
            Implementation.implementations.map { implementation ->
                ScenarioRun(method.name, method, implementation)
            }
        }
        return (eacs + adapterTests + analysisScenarios + generatedScenarios + modellingExamples).toMutableList()
    }

    override fun describeChild(child: ScenarioRun): Description =
        if (excludeImplFromDescription) {
            Description.createTestDescription(testClass.name, child.method.name)
        } else {
            Description.createTestDescription("${child.implementation.name}-${testClass.name}", child.method.name)
        }

    private val excludeImplFromDescription by lazy {
        System.getProperty("excludeImplFromEacDescription")?.toBoolean() == true
    }


}
