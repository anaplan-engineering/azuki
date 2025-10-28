package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.core.system.Action
import com.anaplan.engineering.azuki.core.system.ActionFactory
import com.anaplan.engineering.azuki.core.system.ActionGeneratorFactory
import com.anaplan.engineering.azuki.core.system.CheckFactory
import com.anaplan.engineering.azuki.core.system.QueryFactory
import com.anaplan.engineering.azuki.core.system.UnsupportedAction
import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import kotlin.collections.contains
import kotlin.collections.forEach

/**
 * A fully-assembled scenario script.
 */
abstract class ScenarioScript(val header: String) {

    protected abstract val blocks: List<ScriptBlock>

    /**
     * Renders the contents of the script to a string.
     * The indentation level affects script block delimiters, not the actual contents of the script.
     */
    fun renderInner(indent: Int = 0) = blocks.map { it.render(indent) }

    /**
     * Renders the script and its outer DSL block, without formatting.
     * The indentation level affects script block delimiters, not the actual contents of the script.
     */
    fun renderUnformatted(indent: Int = 0) = object : ScriptBlock(header, renderInner(indent + 1)) {}.render(indent)

    /**
     * Renders the script and its outer DSL block, with KtLint formatting.
     */
    fun renderFormatted() = ScenarioFormatter.formatScenario(renderUnformatted())
}

/**
 * A verifiable scenario (given-when-then) script.
 */
class VerifiableScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val then: ScriptBlock
) : ScenarioScript("verifiableScenario") {

    override val blocks: List<ScriptBlock> get() = listOf(given, whenever, then)
}

class IncompleteScenarioScript(private val given: ScriptBlock, private val whenever: ScriptBlock) {

}

abstract class ScriptBlock(val header: String, val scriptFragments: List<String>) {

    internal fun render(indent: Int = 0) = indenting(indent).let { tab ->
        scriptFragments.joinToString(prefix = "$tab$header {\n", postfix = "\n$tab}", separator = "\n")
    }

    private fun indenting(level: Int): String = "    ".repeat(level)
}
