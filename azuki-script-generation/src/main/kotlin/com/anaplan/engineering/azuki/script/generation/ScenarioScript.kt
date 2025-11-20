package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter

/**
 * A scenario script.
 */
abstract class ScenarioScript(val typeName: String) {

    /**
     * Renders the script.
     *
     * Apply a block to this method to configure the renderer before it renders the script.
     */
    fun render(config: Renderer.() -> Unit = {}) = Renderer().apply(config).render(blocks)

    protected abstract val blocks: List<ScriptBlock>

    /**
     * Customisable renderer for scenario scripts.
     */
    inner class Renderer(var format: Boolean = true, var inOuterBlock: Boolean = true, var indent: Int = 0) {

        internal fun render(blocks: List<ScriptBlock>) = blocks.renderInner().maybeWrap().maybeFormat()

        private fun List<ScriptBlock>.renderInner() = filterNot { it.isEmpty }.map { it.render(innerIndent) }

        private fun List<String>.maybeWrap() = if (inOuterBlock) {
            BasicScriptBlock("${typeName}Scenario", this).render(indent)
        } else {
            joinToString("\n\n")
        }

        private fun String.maybeFormat() = if (format) ScenarioFormatter.formatScenario(this) else this

        private val innerIndent get() = indent + if (inOuterBlock) 1 else 0
    }
}

/**
 * An incomplete result from an oracle (given-whenever, no then)
 */
class IncompleteScenarioScript(private val given: ScriptBlock, private val whenever: ScriptBlock) :
    ScenarioScript("verifiable") {

    override val blocks get() = listOf(given, whenever)
}

/**
 * A verifiable scenario (given-whenever-then) script.
 */
data class VerifiableScenarioScript(
    val given: BasicScriptBlock, val whenever: BasicScriptBlock, val then: BasicScriptBlock
) : ScenarioScript("verifiable") {

    override val blocks get() = listOf(given, whenever, then)
}

/**
 * A query scenario (given-whenever-query) script.
 */
data class QueryScenarioScript(
    val given: BasicScriptBlock, val whenever: BasicScriptBlock, val query: BasicScriptBlock
) : ScenarioScript("query") {

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
) : ScenarioScript("oracle") {

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
