package com.anaplan.engineering.azuki.tictactoe.adapter.scriptgen

import java.util.*

object TicTacToeRunnableScenarioClassGenerator {

    fun generate(
        className: String = "Generated_" + UUID.randomUUID().toString(),
        packageName: String = "",
        implementationVersions: Map<String, String> = emptyMap(),
        scenarioScript: String,
    ) = RunnableScenarioClass(className, packageName, buildString {
        if (packageName.isNotEmpty()) appendLine("package $packageName").appendLine()

        val imports = ticTacToeStandardImports + listOfNotNull("com.anaplan.engineering.azuki.core.runner.*",
            "com.anaplan.engineering.azuki.core.system.*",
            "com.anaplan.engineering.azuki.core.scenario.Since".takeUnless { implementationVersions.isEmpty() },
            "com.anaplan.engineering.azuki.tictactoe.dsl.TicTacToeRunnableScenario")
        imports.sorted().forEach { appendLine("import $it") }
        appendLine()

        appendLine("class ${className.replace("-", "_")} : TicTacToeRunnableScenario() {")
        appendLine()
        appendLine("    @GeneratedScenario")
        if (implementationVersions.isNotEmpty()) {
            appendLine("    @Since(")
            implementationVersions.forEach { (k, v) -> appendLine("""        ImplementationVersion("$k", "$v"),""") }
            appendLine("    )")
        }
        appendLine("    fun test() {")
        appendLine(scenarioScript)
        appendLine("    }")
        appendLine("}")
    })
}

data class RunnableScenarioClass(val className: String, val packageName: String?, val definition: String)

