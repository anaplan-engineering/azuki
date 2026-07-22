package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.reflect.metadata.Importable
import com.anaplan.engineering.azuki.reflect.metadata.ImportSet
import com.anaplan.engineering.azuki.reflect.metadata.asQualifiedName
import com.anaplan.engineering.azuki.reflect.metadata.QualifiedName

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
    val runnableScenarioClassName: QualifiedName,
) {

    fun generate(
        classQualifiedName: QualifiedName,
        implementationVersions: Map<String, String> = emptyMap(),
        scenarioScript: ScenarioScript,
    ) = RunnableScenarioClass(classQualifiedName, buildString {
        classQualifiedName.packageName.let {
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
        if (implementationVersions.isNotEmpty()) importSet += Since::class.asQualifiedName()
        importSet += adapterDslImports
        importSet.imports.forEach { appendLine("import $it") }
        appendLine()

        appendLine("class ${classQualifiedName.simpleName} : ${runnableScenarioClassName.simpleName}() {")
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

data class RunnableScenarioClass(val classQualifiedName: QualifiedName, val definition: String) {

    val className = classQualifiedName.simpleName
    val packageName = classQualifiedName.packageName
}
