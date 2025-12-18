package com.anaplan.engineering.azuki.core.runner

import com.anaplan.engineering.azuki.core.JvmSystemProperties.excludeImplFromEacDescriptionPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.forceKnownBugsPropertyName
import com.anaplan.engineering.azuki.core.JvmSystemProperties.junitTimeoutPropertyName
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.scenario.VerifiableScenario
import com.anaplan.engineering.azuki.core.system.*
import org.junit.AssumptionViolatedException
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
import java.text.MessageFormat
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws
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
    val timeout: Long, val timeUnit: TimeUnit
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

enum class JUnitScenarioType {
    Eac,
    ModellingExample,
    AdapterTest,
    AnalysisScenario,
    GeneratedScenario,
}

enum class JUnitScenarioResult(val category: Category) {
    Verified(Category.Pass),
    Unverified(Category.Fail),
    KnownBug(Category.Skip),
    ToBeDone(Category.Skip),
    Unsupported(Category.Skip),
    IncompatibleVersion(Category.Skip),
    UnsupportedCommand(Category.Skip),
    UnsupportedDeclaration(Category.Skip),
    UnsupportedCheck(Category.Skip),
    NoSupportedChecks(Category.Skip),
    UnexpectedSkip(Category.Fail),
    UnknownError(Category.Error),
    InvalidScenario(Category.Error),
    InvalidSystem(Category.Error);

    enum class Category {
        Pass,
        Fail,
        Skip,
        Error
    }
}

data class ScenarioRun<
    AF : ActionFactory,
    CF : CheckFactory,
    QF : QueryFactory,
    AGF : ActionGeneratorFactory,
    >(
    val name: String,
    val type: JUnitScenarioType,
    val method: FrameworkMethod,
    val implementationInstance: ImplementationInstance<AF, CF, QF, AGF>,
    val persistenceVerificationInstance: ImplementationInstance<AF, CF, QF, AGF>?,
    val eacMetadata: EacMetadata? = null,
    val ignoreWhenUnsupported: Boolean = true,
    val expectSkip: Boolean = false,
    val parameters: Array<Any>? = null,
    val descriptionFormat: String? = null,
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

        internal fun String.matches(pattern: String) = if (pattern.endsWith('*')) {
            this.startsWith(pattern.dropLast(1))
        } else {
            this == pattern
        }

        internal fun Array<out Issue>.anyMatches(name: String) = any { name.matches(it.implementation) }
        internal fun Array<out String>.anyMatches(name: String) = any { name.matches(it) }
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
        val knownBug =
            child.method.getAnnotation(KnownBug::class.java) ?: child.parameters?.filterIsInstance<KnownBug>()
                ?.singleOrNull()
        val implementationName = child.implementationInstance.implementationName
        if (knownBug != null && !runKnownBugs && knownBug.issues.anyMatches(implementationName)) {
            Log.warn("Skipping ${child.method.declaringClass.name}.${child.method.name} as this exhibits a known bug in $implementationName")
            return true
        }
        val toBeDone =
            child.method.getAnnotation(ToBeDone::class.java) ?: child.parameters?.filterIsInstance<ToBeDone>()
                ?.singleOrNull()
        if (toBeDone != null && toBeDone.issues.anyMatches(implementationName)) {
            Log.warn("Skipping ${child.method.declaringClass.name}.${child.method.name} as this is still TBD in $implementationName")
            return true
        }
        val unsupported =
            child.method.getAnnotation(Unsupported::class.java) ?: child.parameters?.filterIsInstance<Unsupported>()
                ?.singleOrNull()
        if (unsupported != null && unsupported.implementation.anyMatches(implementationName)) {
            Log.warn("Skipping ${child.method.declaringClass.name}.${child.method.name} as unsupported in $implementationName")
            return true
        }
        val restrictTo =
            child.method.getAnnotation(RestrictTo::class.java) ?: child.parameters?.filterIsInstance<RestrictTo>()
                ?.singleOrNull()
        return restrictTo != null && !implementationName.matches(restrictTo.implementationName)
    }

    override fun run(notifier: RunNotifier) {
        if (AzukiJUnitRunListener.hasListeners) {
            notifier.addListener(AzukiJUnitRunListener)
        }
        super.run(notifier)
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

    private fun createRunStatement(run: ScenarioRun<AF, CF, QF, AGF>) = ScenarioInvoker(
        run.method.method,
        kClass,
        run.implementationInstance,
        run.persistenceVerificationInstance,
        run.eacMetadata,
        run.ignoreWhenUnsupported,
        run.expectSkip,
        run.parameters,
    )

    private class ScenarioInvoker<AF : ActionFactory, CF : CheckFactory, QF : QueryFactory, AGF : ActionGeneratorFactory, S : VerifiableScenario<AF, CF>>(
        val build: Method,
        val testClass: KClass<S>,
        val implementationInstance: ImplementationInstance<AF, CF, QF, AGF>,
        val persistenceVerificationInstance: ImplementationInstance<AF, CF, QF, AGF>?,
        val eacMetadata: EacMetadata?,
        val ignoreWhenUnsupported: Boolean,
        val expectSkip: Boolean,
        val parameters: Array<Any>?,
    ) : Statement() {
        override fun evaluate() {
            object : ReflectiveCallable() {
                override fun runReflectiveCall(): Any {
                    try {
                        val nonSpecialParameters =
                            parameters?.filterNot { it is Since || it is KnownBug || it is ToBeDone || it is Unsupported || it is RestrictTo }
                                ?.toTypedArray()
                        val scenario = if (nonSpecialParameters.isNullOrEmpty()) {
                            testClass.primaryConstructor!!.call()
                        } else {
                            testClass.primaryConstructor!!.call(*nonSpecialParameters)
                        }

                        checkImplementationSatisfiesSince(scenario)
                        build.invoke(scenario)
                        val scenarioName = eacMetadata?.scenarioName ?: "${build.declaringClass.name}-${build.name}"
                        val verifiableScenarioRunner = VerifiableScenarioRunner(implementationInstance,
                            persistenceVerificationInstance,
                            scenario,
                            scenarioName)
                        when (verifiableScenarioRunner.run()) {
                            VerifiableScenarioRunner.Result.UnsupportedCommand -> unsupported("Skipping - unsupported action found",
                                ::UnsupportedCommandException)

                            VerifiableScenarioRunner.Result.UnsupportedDeclaration -> unsupported("Skipping - unsupported declaration found",
                                ::UnsupportedDeclarationException)

                            VerifiableScenarioRunner.Result.UnsupportedCheck -> unsupported("Skipping - unsupported check found",
                                ::UnsupportedCheckException)

                            VerifiableScenarioRunner.Result.NoSupportedChecks -> unsupported("Skipping - no supported checks found",
                                ::NoSupportedChecksException)

                            VerifiableScenarioRunner.Result.Unverified -> throw AssertionError("Verification checks failed")
                            VerifiableScenarioRunner.Result.IncompatibleSystem -> throw ScenarioRunException("Skipping - system does not support verify or report",
                                JUnitScenarioResult.InvalidSystem)

                            VerifiableScenarioRunner.Result.UnknownError -> throw ScenarioRunException("Unexpected error running scenario",
                                JUnitScenarioResult.UnknownError)

                            VerifiableScenarioRunner.Result.NotPersistable -> throw ScenarioRunException("Invalid configuration: unpersistable system found",
                                JUnitScenarioResult.InvalidSystem)

                            VerifiableScenarioRunner.Result.NotVerifiable -> throw ScenarioRunException("Invalid scenario: not verifiable",
                                JUnitScenarioResult.InvalidScenario)

                            VerifiableScenarioRunner.Result.Verified, VerifiableScenarioRunner.Result.Reported -> {
                            } // success!
                        }
                        if (expectSkip) {
                            throw AssertionError("Scenario should have been skipped, but was not")
                        }
                        if (EacMetadataRecorder.recording && eacMetadata != null) {
                            EacMetadataRecorder.record(eacMetadata)
                        }
                    } catch (e: Throwable) {
                        if (e !is UnexpectedSkipException || !expectSkip) {
                            throw e
                        }
                    }
                    return true
                }
            }.run()
        }

        @Throws(IncompatibleVersionException::class, UnexpectedSkipException::class)
        private fun checkImplementationSatisfiesSince(scenario: S) {
            implementationInstance.checkImplementationSatisfiesVersions(scenario, "implementation instance")
            persistenceVerificationInstance?.checkImplementationSatisfiesVersions(scenario,
                "persistence verification implementation instance")
        }

        @Throws(IncompatibleVersionException::class, UnexpectedSkipException::class)
        private fun ImplementationInstance<AF, CF, QF, AGF>.checkImplementationSatisfiesVersions(
            scenario: S, description: String
        ) {
            val failure = runTask(TaskType.CheckVersion, scenario) { implementation ->
                sinceVersionConstraints.find { !implementation.versionFilter.canVerify(it.version) }
            }.result
            if (failure != null) {
                incompatibleVersion("Skipping - scenario version ${failure.version} incompatible with $description")
            }
        }

        private val sinceVersionConstraints by lazy {
            /* A scenario can have two possible version constraints: one on the method itself, and another coming from
             * its parameter set (if it's a parameterized test).  Since we can't rely on there being a particular
             * versioning convention on the implementation, we need to check that the implementation satisfies both
             * constraints if both are present.
             */
            val sources = listOf("method" to build.getAnnotation(Since::class.java),
                "params" to parameters?.filterIsInstance<Since>()?.singleOrNull())
            sources.mapNotNull { (source, iv) ->
                iv?.implementationVersion?.singleOrNull {
                    implementationInstance.implementationName.matches(it.name)
                }?.also {
                    Log.debug("Since version from {}: {} on {}", source, it.version, it.name)
                }
            }.toSet()
        }

        private fun incompatibleVersion(msg: String) = unsupported(msg, ::IncompatibleVersionException)

        private fun unsupported(msg: String, aveCreator: (msg: String) -> AssumptionViolatedException) {
            // was previously checking if implementation was total as part of this.. should we move that into runner?
            if (ignoreWhenUnsupported) {
                Log.debug(msg)
                throw aveCreator(msg)
            } else {
                throw UnexpectedSkipException(msg)
            }
        }

    }

    internal interface ScenarioResultHolder {
        val result: JUnitScenarioResult
    }

    internal class ScenarioRunException(msg: String, override val result: JUnitScenarioResult) : Exception(msg),
        ScenarioResultHolder

    internal abstract class ScenarioUnsupportedException(msg: String, override val result: JUnitScenarioResult) :
        AssumptionViolatedException(msg), ScenarioResultHolder

    internal class UnsupportedDeclarationException(msg: String) :
        ScenarioUnsupportedException(msg, JUnitScenarioResult.UnsupportedDeclaration)

    internal class UnsupportedCommandException(msg: String) :
        ScenarioUnsupportedException(msg, JUnitScenarioResult.UnsupportedCommand)

    internal class UnsupportedCheckException(msg: String) :
        ScenarioUnsupportedException(msg, JUnitScenarioResult.UnsupportedCheck)

    internal class NoSupportedChecksException(msg: String) :
        ScenarioUnsupportedException(msg, JUnitScenarioResult.NoSupportedChecks)

    internal class IncompatibleVersionException(msg: String) :
        ScenarioUnsupportedException(msg, JUnitScenarioResult.IncompatibleVersion)

    internal class UnexpectedSkipException(msg: String) : Exception(msg), ScenarioResultHolder {
        override val result: JUnitScenarioResult = JUnitScenarioResult.UnexpectedSkip
    }

    val parameterMethod: FrameworkMethod? by lazy {
        val companionObject = kClass.companionObjectInstance
        if (companionObject == null) {
            null
        } else {
            val parameterMethods = TestClass(companionObject.javaClass).getAnnotatedMethods(Parameters::class.java)
            Log.debug("Parameter methods: {}", parameterMethods)
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
        Log.debug("Getting children: {}", testClass)
        val implementationInstances = ImplementationInstance.getImplementationInstances<AF, CF, QF, AGF>()
        val persistenceVerificationInstance = if (ImplementationInstance.havePersistenceVerificationInstance) {
            ImplementationInstance.getPersistenceVerificationInstance<AF, CF, QF, AGF>()
        } else {
            null
        }
        Log.debug("Available implementation instances: {}", implementationInstances)
        Log.debug("Persistent verification instance: {}", persistenceVerificationInstance ?: "Not specified")
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
                    JUnitScenarioType.Eac,
                    method,
                    implementationInstance,
                    persistenceVerificationInstance,
                    eacMetadata = eacMetadata)
            }
        }
        val modellingExamples = getTestClass().getAnnotatedMethods(ModellingExample::class.java).flatMap { method ->
            val modellingExample = method.getAnnotation(ModellingExample::class.java)!!
            implementationInstances.map { implementationInstance ->
                ScenarioRun(modellingExample.summary,
                    JUnitScenarioType.ModellingExample,
                    method,
                    implementationInstance,
                    persistenceVerificationInstance)
            }
        }
        val adapterTests = getTestClass().getAnnotatedMethods(AdapterTest::class.java).flatMap { method ->
            val adapterTest = method.getAnnotation(AdapterTest::class.java)!!
            implementationInstances.map { implementationInstance ->
                ScenarioRun(method.name,
                    JUnitScenarioType.AdapterTest,
                    method,
                    implementationInstance,
                    persistenceVerificationInstance,
                    ignoreWhenUnsupported = false,
                    expectSkip = adapterTest.expectSkip)
            }
        }
        val analysisScenarios = getTestClass().getAnnotatedMethods(AnalysisScenario::class.java).flatMap { method ->
            implementationInstances.map { implementationInstance ->
                ScenarioRun(method.name,
                    JUnitScenarioType.AnalysisScenario,
                    method,
                    implementationInstance,
                    persistenceVerificationInstance)
            }
        }
        val generatedScenarios = getTestClass().getAnnotatedMethods(GeneratedScenario::class.java).flatMap { method ->
            implementationInstances.map { implementationInstance ->
                ScenarioRun(method.name,
                    JUnitScenarioType.GeneratedScenario,
                    method,
                    implementationInstance,
                    persistenceVerificationInstance)
            }
        }
        val nonParameterizedRuns = eacs + adapterTests + analysisScenarios + generatedScenarios + modellingExamples
        return (if (parameterMethod == null) nonParameterizedRuns else parameterize(nonParameterizedRuns)).toMutableList()
    }

    private fun parameterize(baseRuns: List<ScenarioRun<AF, CF, QF, AGF>>): List<ScenarioRun<AF, CF, QF, AGF>> {
        val perms = getParameterPermutations()
        Log.debug("Test is parameterized, parameter method: ${parameterMethod?.name}, permutation count: ${perms.size}")
        val descriptionFormat = parameterMethod!!.annotations.filterIsInstance<Parameters>().singleOrNull()?.name
        return baseRuns.flatMap { baseRun ->
            perms.map { perm ->
                baseRun.copy(parameters = perm,
                    descriptionFormat = if (descriptionFormat == "{index}") null else descriptionFormat)
            }
        }
    }

    private fun getParameterPermutations(): Collection<Array<Any>> {
        val methodResult = parameterMethod!!.method.invoke(kClass.companionObjectInstance!!)
        check(methodResult is Collection<*>) { "Parameter method $parameterMethod returns object with invalid type" }
        val perms = methodResult.filterIsInstance<Array<Any>>()
        check(perms.size == methodResult.size) { "Parameter method $parameterMethod returns collection with invalid element type" }
        return perms
    }

    override fun describeChild(child: ScenarioRun<AF, CF, QF, AGF>): Description {
        val parameterSuffix = if (child.parameters == null) {
            ""
        } else if (child.descriptionFormat != null) {
            " <- ${MessageFormat.format(child.descriptionFormat, *child.parameters)}"
        } else {
            "${child.parameters.toList()}"
        }
        val methodName = "${child.method.name}$parameterSuffix"
        val implementationName = child.implementationInstance.implementationName

        val issues = mutableListOf<Issue>()
        val toBeDone =
            child.method.getAnnotation(ToBeDone::class.java) ?: child.parameters?.filterIsInstance<ToBeDone>()
                ?.singleOrNull()
        val hasToBeDone = toBeDone != null
        val tbdIssues = toBeDone?.issues?.filter { implementationName.matches(it.implementation) }
        if (tbdIssues != null) issues.addAll(tbdIssues)
        val knownBug =
            child.method.getAnnotation(KnownBug::class.java) ?: child.parameters?.filterIsInstance<KnownBug>()
                ?.singleOrNull()
        val hasKnownBug = knownBug != null
        val kbIssues = knownBug?.issues?.filter { implementationName.matches(it.implementation) }
        if (kbIssues != null) issues.addAll(kbIssues)
        val annotations =
            mutableListOf(TestImplementation(implementationName, child.implementationInstance.version ?: ""),
                ScenarioInfo(testClass.name,
                    methodName,
                    child.type,
                    hasKnownBug,
                    hasToBeDone,
                    issues.flatMap { it.jiraIds.toList() }.toTypedArray())).apply {
                child.persistenceVerificationInstance.let {
                    if (it != null) {
                        add(PersistenceImplementation(it.implementationName, it.version ?: ""))
                    }
                }
            }.toTypedArray()

        return if (excludeImplFromDescription) {
            Description.createTestDescription(testClass.name, methodName, *annotations)
        } else {
            Description.createTestDescription("$implementationName-${testClass.name}", methodName, *annotations)
        }
    }

    private val excludeImplFromDescription by lazy {
        System.getProperty(excludeImplFromEacDescriptionPropertyName, "false").toBoolean()
    }

}
