package com.anaplan.engineering.azuki.core.parser

import com.anaplan.engineering.azuki.core.scenario.BuildableScenario

interface ScenarioParser<S : BuildableScenario<*>> {
    /**
     * Parses a scenario from a string and additional imports prefix.
     *
     * New implementors of ScenarioParser should implement this by calling
     * `parse(scenarioString) { requireImportsFromString(requiredImports) }`.
     */
    @Deprecated(
        "Passing required imports as a string is deprecated and may disappear in a future major revision",
        ReplaceWith(
            "parse(scenarioString) { requireImportsFromString(requiredImports) }",
            "com.anaplan.engineering.azuki.core.parser.ScenarioParser",
            "com.anaplan.engineering.azuki.core.parser.ScenarioParsingContext"
        )
    )
    fun parse(
        scenarioString: String, requiredImports: String
    ): S

    /**
     * Parses a scenario from a string.
     *
     * Supply extra implicit imports to include into the script as a lambda
     * expression following the scenario itself.
     *
     * New implementors of ScenarioParser should implement this method
     * directly.
     */
    fun parse(
        scenarioString: String, initContext: ScenarioParsingContext.() -> Unit
    ): S {
        val requiredImports = ScenarioParsingContext().apply(initContext).toImportString()

        @Suppress("DEPRECATION")
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
