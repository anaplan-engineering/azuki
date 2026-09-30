package com.anaplan.engineering.azuki.script.formatter

import io.github.ktlint.core.rule.engine.api.*
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.ruleset.standard.StandardRuleSetProvider
import java.io.File

/**
 * Formats scenario scripts.
 */
object ScenarioFormatter {

    private val ruleProviders = StandardRuleSetProvider().getRuleProviders()

    private val editorConfigPropertyRegistry = EditorConfigPropertyRegistry(ruleProviders)

    private val editorConfigOverride = EditorConfigOverride.from(
        EXPERIMENTAL_RULES_EXECUTION_PROPERTY to RuleExecution.enabled,
        editorConfigPropertyRegistry.find("ktlint_standard_argument-list-wrapping") to RuleExecution.disabled,
    )

    private val ruleEngine = KtLintRuleEngine(
        ruleProviders = ruleProviders,
        editorConfigOverride = editorConfigOverride,
    )

    @JvmStatic
    fun formatScenario(scenarioText: String) = runKtLint(Code.fromSnippet(scenarioText, script = true))

    private fun runKtLint(code: Code) = ruleEngine.format(code) { error ->
        if (error.canBeAutoCorrected) AutocorrectDecision.ALLOW_AUTOCORRECT else AutocorrectDecision.NO_AUTOCORRECT
    }
}

fun main(args: Array<String>) {
    val fileName = args[0]
    val scenarioText = File(fileName).readText()

    println(ScenarioFormatter.formatScenario(scenarioText))
}
