package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario

interface ScenarioParser<S : BuildableScenario<*>> {

    fun parse(
        scenarioString: String, requiredImports: String
    ): S

    /**
     * Parses a scenario from a string.
     *
     * Supply extra implicit imports to include into the script as a lambda
     * expression following the scenario itself.
     */
    fun parse(
        scenarioString: String, init: ScenarioParsingContext.() -> Unit
    ): S {
        val requiredImports = ScenarioParsingContext().apply(init).toImportString()

        return parse(scenarioString, requiredImports)
    }
}

class ScenarioParsingContext(private val requiredImports: MutableList<String> = mutableListOf()) {
    val imports: List<String> get() = requiredImports

    fun requireImportsFromString(string: String) {
        val toAdd = string.split("\n").map {
            it.trim().removePrefix("import").trim()
        }.filter { it.isNotBlank() }

        requiredImports.addAll(toAdd)
    }

    fun import(vararg imports: String) {
        requiredImports.addAll(imports)
    }

    fun toImportString(): String = requiredImports.joinToString("\n") { "import $it" }
}
