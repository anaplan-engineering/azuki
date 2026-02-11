package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.script.generation.runnable.ImportSet
import com.anaplan.engineering.azuki.script.generation.runnable.Importable
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier
import com.anaplan.engineering.azuki.script.generation.runnable.QualifiedIdentifier.Companion.toQualifiedIdentifier

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
        classQualifiedIdentifier: QualifiedIdentifier,
        implementationVersions: Map<String, String> = emptyMap(),
        scenarioScript: ScenarioScript,
    ) = RunnableScenarioClass(classQualifiedIdentifier, buildString {
        classQualifiedIdentifier.packageName.let {
            if (it.isNotEmpty()) {
                appendLine("package $it").appendLine()
            }
        }

        // Do *not* import `classQualifiedIdentifier`, as that'll create a self-referential import
        val importSet = ImportSet(
            Importable.wildcard("com.anaplan.engineering.azuki.core.runner"),
            Importable.wildcard("com.anaplan.engineering.azuki.core.system"),
            runnableScenarioClassName,
        )
        if (implementationVersions.isNotEmpty()) importSet += Since::class.toQualifiedIdentifier()
        importSet += adapterDslImports
        importSet.imports.forEach { appendLine("import $it") }
        appendLine()

        appendLine("class ${classQualifiedIdentifier.identifier} : ${runnableScenarioClassName.identifier}() {")
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

data class RunnableScenarioClass(val classQualifiedIdentifier: QualifiedIdentifier, val definition: String) {

    val className = classQualifiedIdentifier.identifier
    val packageName = classQualifiedIdentifier.packageName
}
