package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.GeneratedScenario
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import com.anaplan.engineering.azuki.script.generation.VerifiableScenarioScript
import com.anaplan.engineering.azuki.script.generation.runnable.RunnableScenarioClassRenderer.Companion.render

/**
 * Generates full Kotlin JUnit runnable scenario classes given a scenario script.
 *
 * This is a wrapper around the more general-purpose Azuki infrastructure for runnable scenario script generation,
 * intended for the most common purpose of lifting one scenario into one test method.
 */
open class RunnableScenarioClassGenerator(
    /**
     * Any imports that are needed to bring in the adapter's `RunnableScenario` class and associated DSL.
     */
    val adapterDslImports: List<Importable>,
    /**
     * The name of the base `RunnableScenario` class for this adapter.
     */
    val runnableScenarioClassName: QualifiedIdentifier,
) {

    /**
     * Generates a runnable scenario given its script.
     *
     * If `implementationVersions` is given, it will generate a `@Since` annotation on the test method.
     */
    fun generate(
        scenarioScript: VerifiableScenarioScript,
        testName: QualifiedIdentifier,
        implementationVersions: Map<String, String> = emptyMap(),
    ): RunnableScenarioClass {
        val script = generateScript(testName, implementationVersions, scenarioScript)
        val definition = ScenarioFormatter.formatScenario(script.render {
            imports += adapterDslImports
        })
        return RunnableScenarioClass(script.testName.identifier, script.testName.packageName, definition)
    }

    private fun generateScript(
        testName: QualifiedIdentifier, implementationVersions: Map<String, String>, scenarioScript: VerifiableScenarioScript
    ) = RunnableScenarioClassScript(testName,
        runnableScenarioClassName,
        listOf(generateMethod(implementationVersions, scenarioScript)))

    private fun generateMethod(
        implementationVersions: Map<String, String>, scenarioScript: VerifiableScenarioScript
    ) = RunnableScenarioClassScript.MethodScript("test",
        GeneratedScenario().toMethodType(),
        RunnableScenarioAnnotations(since = generateSince(implementationVersions)),
        scenarioScript)

    private fun generateSince(
        implementationVersions: Map<String, String>
    ) = implementationVersions.takeUnless { it.isEmpty() }?.map { (name, version) ->
        ImplementationVersion(name, version)
    }?.let { Since(*it.toTypedArray()) }
}

data class RunnableScenarioClass(val className: String, val packageName: String?, val definition: String)
