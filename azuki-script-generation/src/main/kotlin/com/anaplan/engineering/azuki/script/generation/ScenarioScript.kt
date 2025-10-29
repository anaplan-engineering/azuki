package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter

/**
 * A partially-assembled scenario script.
 */
abstract class ScenarioScript {

    protected abstract val blocks: List<ScriptBlock>

    /**
     * Renders the contents of the script to a string.
     * The indentation level affects script block delimiters, not the actual contents of the script.
     */
    fun renderInner(indent: Int = 0) = blocks.map { it.render(indent) }
}

/**
 * An incomplete result from an oracle (given-whenever, no then)
 */
class IncompleteScenarioScript(private val given: ScriptBlock, private val whenever: ScriptBlock) : ScenarioScript() {

    override val blocks: List<ScriptBlock> get() = listOf(given, whenever)
}

/**
 * A fully-assembled scenario script.
 */
abstract class FullScenarioScript(val typeName: String) : ScenarioScript() {

    /**
     * Renders the script and its outer DSL block, without formatting.
     * The indentation level affects script block delimiters, not the actual contents of the script.
     */
    fun renderUnformatted(indent: Int = 0) = object : ScriptBlock("${typeName}Scenario", renderInner(indent + 1)) {}.render(indent)

    /**
     * Renders the script and its outer DSL block, with KtLint formatting.
     */
    fun renderFormatted() = ScenarioFormatter.formatScenario(renderUnformatted())
}

/**
 * A verifiable scenario (given-whenever-then) script.
 */
class VerifiableScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val then: ScriptBlock
) : FullScenarioScript("verifiable") {

    override val blocks: List<ScriptBlock> get() = listOf(given, whenever, then)
}

/**
 * A query scenario (given-whenever-query) script.
 */
class QueryScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val query: ScriptBlock
) : FullScenarioScript("query") {

    override val blocks: List<ScriptBlock> get() = listOf(given, whenever, query)
}


abstract class ScriptBlock(val header: String, val scriptFragments: List<String>) {

    internal fun render(indent: Int = 0) = indenting(indent).let { tab ->
        scriptFragments.joinToString(prefix = "$tab$header {\n", postfix = "\n$tab}", separator = "\n")
    }

    private fun indenting(level: Int): String = "    ".repeat(level)
}
