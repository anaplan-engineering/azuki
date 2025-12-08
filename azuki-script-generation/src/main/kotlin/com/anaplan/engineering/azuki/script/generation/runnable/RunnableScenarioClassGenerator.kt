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
    val adapterSpecificImports: List<KotlinName>,
    val baseClassName: KotlinName,
) {

    /**
     * Generates a runnable scenario given its script.
     *
     * If `className` is given, it will be used for the test class name and package; otherwise, the class will get an
     * arbitrary pseudorandomly-generated name and no package.
     *
     * If `implementationVersions` is given, it will generate a `@Since` annotation on the test method.
     */
    fun generate(
        scenarioScript: VerifiableScenarioScript,
        className: KotlinName? = null,
        implementationVersions: Map<String, String> = emptyMap(),
    ): RunnableScenarioClass {
        val script = generateScript(className, implementationVersions, scenarioScript)
        val definition = ScenarioFormatter.formatScenario(script.render {
            extraImports += adapterSpecificImports
        })
        return RunnableScenarioClass(script.testName.identifier, script.testName.packageName, definition)
    }

    private fun generateScript(
        className: KotlinName?, implementationVersions: Map<String, String>, scenarioScript: VerifiableScenarioScript
    ) = RunnableScenarioClassScript(testName = className ?: KotlinName.generateArbitrary(""),
        baseClassName,
        listOf(generateMethod(implementationVersions, scenarioScript)))

    private fun generateMethod(
        implementationVersions: Map<String, String>, scenarioScript: VerifiableScenarioScript
    ) = RunnableScenarioClassScript.MethodScript("test",
        GeneratedScenarioMethodType(GeneratedScenario()),
        RunnableScenarioAnnotations(since = generateSince(implementationVersions)),
        scenarioScript)

    private fun generateSince(
        implementationVersions: Map<String, String>
    ) = implementationVersions.takeUnless({ it.isEmpty() })?.map { (name, version) ->
        ImplementationVersion(name, version)
    }?.let { Since(*it.toTypedArray()) }
}

data class RunnableScenarioClass(val className: String, val packageName: String?, val definition: String)
