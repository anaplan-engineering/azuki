package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter

/**
 * A scenario script.
 */
abstract class ScenarioScript(val typeName: String, val blocks: ScriptElementList<ScriptBlock>) {

    /**
     * Renders the script.
     *
     * Apply a block to this method to configure the renderer before it renders the script.
     */
    fun render(config: Renderer.() -> Unit = {}) = Renderer().apply(config).render(blocks)

    /**
     * Customisable renderer for scenario scripts.
     */
    inner class Renderer(var useFormatter: Boolean = true, var inOuterBlock: Boolean = true, var indent: Int = 0) {

        internal fun render(blocks: ScriptElementList<ScriptBlock>) = blocks.doIf(inOuterBlock) {
            ScriptBlock("${typeName}Scenario", it)
        }.render(RenderContext(indent)).doIf(useFormatter) {
            ScenarioFormatter.formatScenario(it)
        }

        private fun <O, I : O> I.doIf(cond: Boolean, f: (I) -> O) = if (cond) f(this) else this
    }
}

/**
 * An incomplete result from an oracle (given-whenever, no then)
 */
data class IncompleteScenarioScript(val given: ScriptBlock, val whenever: ScriptBlock) :
    ScenarioScript("verifiable", ScriptElementList(given, whenever))

/**
 * A verifiable scenario (given-whenever-then) script.
 */
data class VerifiableScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val then: ScriptBlock
) : ScenarioScript("verifiable", ScriptElementList(given, whenever, then))

/**
 * A query scenario (given-whenever-query) script.
 */
data class QueryScenarioScript(
    val given: ScriptBlock, val whenever: ScriptBlock, val query: ScriptBlock
) : ScenarioScript("query", ScriptElementList(given, whenever, query))

/**
 * An oracle scenario (given-generate-whenever-generate-verify) script.
 */
data class OracleScenarioScript(
    val given: ScriptBlock,
    val givenGenerate: ScriptElementList<ScriptBlock>,
    val whenever: ScriptBlock,
    val whenGenerate: ScriptElementList<ScriptBlock>,
    val verify: ScriptBlock
) : ScenarioScript("oracle", ScriptElementList(buildList {
    add(given)
    addAll(givenGenerate.elements)
    add(whenever)
    addAll(whenGenerate.elements)
    add(verify)
}))

/**
 * A component in a DSL script that is being generated.
 */
interface ScriptElement {

    /**
     * Is the element empty and therefore safe to skip?
     */
    val isEmpty: Boolean

    /**
     * Renders the contents of the script element to a string with the given context.
     */
    fun render(ctx: RenderContext = RenderContext()): String
}

/**
 * A raw string fragment as a script element.
 *
 * Because we don't know anything about the contents of these elements, we can't inspect them or safely reformat them
 * (aside from passing the end result to a general Kotlin formatter).  This means that they ignore indentation hints,
 * for example.
 */
data class ScriptStringFragment(val fragment: String) : ScriptElement {

    override val isEmpty: Boolean = fragment.isBlank()

    override fun render(ctx: RenderContext) = fragment
}

/**
 * A standard DSL script block with a header.
 */
open class ScriptBlock(val header: String, val inner: ScriptElement) : ScriptElement {

    constructor(header: String, contents: List<ScriptElement>) : this(header, ScriptElementList(contents))

    override val isEmpty get() = inner.isEmpty

    /**
     * Gets the script elements contained within this block.
     */
    val elements get() = if (inner is ScriptElementList<*>) inner.elements else listOf(inner)

    override fun render(ctx: RenderContext) = with(ctx) {
        """
        $tab$header {
        ${inner.render(nextIndent)}
        $tab}
        """.trimIndent()
    }
}

/**
 * A script element containing zero or more vertically joined script elements.
 */
open class ScriptElementList<out E : ScriptElement>(val elements: List<E>) : ScriptElement {

    constructor(vararg elements: E) : this(listOf(*elements))

    override val isEmpty get() = elements.all { it.isEmpty }

    override fun render(ctx: RenderContext) = elements.filterNot { it.isEmpty }.joinToString("\n") { it.render(ctx) }
}

/**
 * Context about how to render a script element, which is threaded through the rendering process.
 */
data class RenderContext(
    val indent: Int = 0
    // TODO: we might need other things here such as target line width
    // TODO: should this be parameterised on implementation-specific data?
) {

    /**
     * Creates a new context for the next indent level.
     */
    val nextIndent by lazy { copy(indent = indent + 1) }

    /**
     * A number of spaces corresponding to the indent level.
     */
    val tab by lazy { "    ".repeat(indent) }
}
