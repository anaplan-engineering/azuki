package com.anaplan.engineering.azuki.script.formatter.ktlint

import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatterService
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride
import com.pinterest.ktlint.rule.engine.api.EditorConfigPropertyRegistry
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RuleExecution
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import java.io.File

/**
 * Formats using the version of ktlint tracked by Azuki.
 *
 * Note that, while this can be consumed outside Azuki, it tracks the same
 * version of the Kotlin environment as Azuki, and is known to cause conflicts
 * if invoked from a Kotlin environment with an incompatible language version.
 */
class KtLintScenarioFormatterService : ScenarioFormatterService {

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

    override fun formatScenario(scenarioText: String): String {
        // Workaround for https://github.com/pinterest/ktlint/issues/3220: KtLint assumes there is either no U+FEFF,
        // or the first one is a BOM (ie at the start of the file).  If the first U+FEFF turns up elsewhere, it deletes
        // it but doesn't reinsert it.  So we just add a BOM in all cases to force the correct behavior.
        val hadBom = scenarioText.startsWith(BOM)
        val code = Code.fromSnippet(if (hadBom) scenarioText else "${BOM}$scenarioText", true)
        val formatted = runKtLint(code)
        // make sure we drop the BOM if it wasn't one we spliced in
        return if (hadBom) formatted else formatted.removePrefix(BOM)
    }

    private fun runKtLint(code: Code) = ruleEngine.format(code) { error ->
        if (error.canBeAutoCorrected) AutocorrectDecision.ALLOW_AUTOCORRECT else AutocorrectDecision.NO_AUTOCORRECT
    }
}

private const val BOM = "\uFEFF"

fun main(args: Array<String>) {
    val fileName = args[0]
    val scenarioText = File(fileName).readText()

    println(KtLintScenarioFormatterService().formatScenario(scenarioText))
}
