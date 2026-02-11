package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.script.generation.runnable.ImportSet
import com.anaplan.engineering.azuki.script.generation.runnable.Importable
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier
import java.util.*

/**
 * Generates full Kotlin JUnit runnable scenario classes given a scenario script.
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

    fun generate(
        className: String = "Generated_${UUID.randomUUID()}",
        packageName: String = "",
        implementationVersions: Map<String, String> = emptyMap(),
        scenarioScript: ScenarioScript,
    ) = RunnableScenarioClass(className, packageName, buildString {
        if (packageName.isNotEmpty()) appendLine("package $packageName").appendLine()

        val importSet = ImportSet(
            Importable.wildcard("com.anaplan.engineering.azuki.core.runner"),
            Importable.wildcard("com.anaplan.engineering.azuki.core.system"),
        )
        if (implementationVersions.isNotEmpty()) importSet += Since::class.toQualifiedIdentifier()
        adapterDslImports.forEach { importSet += it }
        importSet.imports.forEach { appendLine("import $it") }
        appendLine()

        appendLine("class ${className.replace("-", "_")} : ${runnableScenarioClassName.identifier}() {")
        appendLine()
        appendLine("    @GeneratedScenario")
        if (implementationVersions.isNotEmpty()) {
            appendLine("    @Since(")
            implementationVersions.forEach { (k, v) -> appendLine("""        ImplementationVersion("$k", "$v"),""") }
            appendLine("    )")
        }
        appendLine("    fun test() {")
        appendLine(scenarioScript.render {
            indentLevel = 2
            scriptType = ScriptType.Inline
            // We're going to format the whole test-case anyway, so formatting twice is pointless
            formatter = Formatter.None
        })
        appendLine("    }")
        appendLine("}")
    })
}

data class RunnableScenarioClass(val className: String, val packageName: String?, val definition: String)
