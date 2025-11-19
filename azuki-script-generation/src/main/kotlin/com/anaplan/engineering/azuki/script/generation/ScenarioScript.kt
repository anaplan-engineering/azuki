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
    fun renderInner(indent: Int = 0) = blocks.filterNot { it.isEmpty }.map { it.render(indent) }
}

/**
 * An incomplete result from an oracle (given-whenever, no then)
 */
class IncompleteScenarioScript(private val given: ScriptBlock, private val whenever: ScriptBlock) : ScenarioScript() {

    override val blocks get() = listOf(given, whenever)
}

/**
 * A fully-assembled scenario script.
 */
abstract class FullScenarioScript(val typeName: String) : ScenarioScript() {

    /**
     * Renders the script and its outer DSL block, without formatting.
     * The indentation level affects script block delimiters, not the actual contents of the script.
     */
    fun renderUnformatted(indent: Int = 0) =
        BasicScriptBlock("${typeName}Scenario", renderInner(indent + 1)).render(indent)

    /**
     * Renders the script and its outer DSL block, with KtLint formatting.
     */
    fun renderFormatted() = ScenarioFormatter.formatScenario(renderUnformatted())
}

/**
 * A verifiable scenario (given-whenever-then) script.
 */
data class VerifiableScenarioScript(
    val given: BasicScriptBlock, val whenever: BasicScriptBlock, val then: BasicScriptBlock
) : FullScenarioScript("verifiable") {

    override val blocks get() = listOf(given, whenever, then)
}

/**
 * A query scenario (given-whenever-query) script.
 */
data class QueryScenarioScript(
    val given: BasicScriptBlock, val whenever: BasicScriptBlock, val query: BasicScriptBlock
) : FullScenarioScript("query") {

    override val blocks get() = listOf(given, whenever, query)
}

/**
 * An oracle scenario (given-generate-whenever-generate-verify) script.
 */
data class OracleScenarioScript(
    val given: BasicScriptBlock,
    val givenGenerate: CompositeScriptBlock,
    val whenever: BasicScriptBlock,
    val whenGenerate: CompositeScriptBlock,
    val verify: BasicScriptBlock
) : FullScenarioScript("oracle") {

    override val blocks
        get() = buildList {
            add(given)
            addAll(givenGenerate.subBlocks)
            add(whenever)
            addAll(whenGenerate.subBlocks)
            add(verify)
        }
}

interface ScriptBlock {

    /**
     * Is the block empty and therefore safe to skip?
     */
    val isEmpty: Boolean

    /**
     * Renders the contents of the script block to a string.
     * The indentation level affects block delimiters, not the actual contents of the script.
     */
    fun render(indent: Int = 0): String
}

/**
 * A standard DSL script block, with a header and several lines of DSL.
 */
open class BasicScriptBlock(val header: String, val scriptFragments: List<String>) : ScriptBlock {

    override val isEmpty = scriptFragments.isEmpty()

    override fun render(indent: Int) = indenting(indent).let { tab ->
        scriptFragments.joinToString(prefix = "$tab$header {\n", postfix = "\n$tab}", separator = "\n")
    }

    private fun indenting(level: Int): String = "    ".repeat(level)
}

/**
 * A script block containing zero or more subordinate script blocks.
 */
open class CompositeScriptBlock(val subBlocks: List<ScriptBlock>) : ScriptBlock {

    override val isEmpty = subBlocks.isEmpty()

    override fun render(indent: Int) = subBlocks.joinToString("\n") { it.render(indent) }
}
