package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.runner.RunnableScenario
import kotlin.collections.plus
import kotlin.collections.sorted
import java.util.*
import kotlin.reflect.KClass

/**
 * Generates full Kotlin JUnit runnable scenario classes given a scenario script.
 */
abstract class RunnableScenarioClassGenerator<S : RunnableScenario<*, *, *, *, *, *, *, *, *, *, *, *>>(
    val adapterSpecificImports: List<String>,
    val scenarioClass: KClass<S>,
) {

    fun generate(
        className: String = "Generated_" + UUID.randomUUID().toString(),
        packageName: String = "",
        implementationVersions: Map<String, String> = emptyMap(),
        scenarioScript: ScenarioScript,
    ) = RunnableScenarioClass(className, packageName, buildString {
        if (packageName.isNotEmpty()) appendLine("package $packageName").appendLine()

        val commonImports = listOfNotNull("com.anaplan.engineering.azuki.core.runner.*",
            "com.anaplan.engineering.azuki.core.system.*",
            "com.anaplan.engineering.azuki.core.scenario.Since".takeUnless { implementationVersions.isEmpty() },
            scenarioClass.qualifiedName)
        val imports = commonImports + adapterSpecificImports
        imports.sorted().forEach { appendLine("import $it") }
        appendLine()

        appendLine("class ${className.replace("-", "_")} : ${scenarioClass.simpleName}() {")
        appendLine()
        appendLine("    @GeneratedScenario")
        if (implementationVersions.isNotEmpty()) {
            appendLine("    @Since(")
            implementationVersions.forEach { (k, v) -> appendLine("""        ImplementationVersion("$k", "$v"),""") }
            appendLine("    )")
        }
        appendLine("    fun test() {")
        appendLine(scenarioScript.render {
            indent = 2
            inOuterBlock = true

            // We're going to format the whole test-case anyway, so formatting twice is pointless
            useFormatter = false
        })
        appendLine("    }")
        appendLine("}")
    })
}

data class RunnableScenarioClass(val className: String, val packageName: String?, val definition: String)
